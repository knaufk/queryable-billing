package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.tngtech.qb.Constants.PER_CUSTOMER_STATE_NAME;
import static com.tngtech.qb.Constants.PER_EVENT_TYPE_STATE_NAME;
import static org.joda.money.CurrencyUnit.*;

public class QueryableBillingJob {

  private final StreamExecutionEnvironment env;
  private final ParameterTool parameters;

  private static final Time ONE_MONTH = Time.days(30);

  QueryableBillingJob(
      final StreamExecutionEnvironment env, ParameterTool parameters) {
    this.parameters = parameters;
    this.env = env;
    this.env.enableCheckpointing(10_000);
    this.env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
  }

  public static void main(String[] args) throws Exception {
    ParameterTool parameters = ParameterTool.fromArgs(args);
    new QueryableBillingJob(StreamExecutionEnvironment.getExecutionEnvironment(), parameters).run();
  }

  void run() throws Exception {
    DataStream<BillableEvent> input =
        billableEvents()
            .assignTimestampsAndWatermarks(
                new BoundedOutOfOrdernessTimestampExtractor<BillableEvent>(
                    Time.of(12, TimeUnit.HOURS)) {
                  @Override
                  public long extractTimestamp(final BillableEvent element) {
                    return element.getTimestampMs();
                  }
                });
    process(input);
    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStream<BillableEvent> billableEvents() {
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", parameters.getRequired("bootstrap-servers"));
    properties.setProperty("group.id", "test");
    return env.addSource(
            new FlinkKafkaConsumer010<>(
                Constants.SRC_KAFKA_TOPIC, new SimpleStringSchema(), properties))
        .name("BillableEventSource (Kafka)")
        .flatMap(
            (FlatMapFunction<String, BillableEvent>)
                (value, out) -> {
                  final String[] fields = value.split(",");
                  out.collect(
                      new BillableEvent(
                          Long.valueOf(fields[0]),
                          fields[1],
                          Money.of(EUR, Double.valueOf(fields[2]), RoundingMode.UP),
                          BillableEvent.BillableEventType.valueOf(fields[3].toUpperCase())));
                })
        .returns(BillableEvent.class);
  }

  private void process(DataStream<BillableEvent> billableEvents) {
    outputFinalInvoice(billableEvents);

    exposeQueryableCustomerPreviews(billableEvents);
    exposeQueryableTypePreviews(billableEvents);
  }

  private void outputFinalInvoice(final DataStream<BillableEvent> billableEvents) {
    sumUp(windowByCustomer(billableEvents), Optional.empty())
        .addSink(
            new BucketingSink<MonthlySubtotalByCategory>(parameters.getRequired("output"))
                .setBucketer(new MonthBucketer())
                .setInactiveBucketCheckInterval(10)
                .setInactiveBucketThreshold(10))
        .name("Create Final Invoices");
  }

  private void exposeQueryableCustomerPreviews(final DataStream<BillableEvent> billableEvents) {
    final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar =
        windowByCustomer(billableEvents).trigger(CountTrigger.of(1));
    sumUp(eventsSoFar, Optional.of(PER_CUSTOMER_STATE_NAME));
  }

  private WindowedStream<BillableEvent, String, TimeWindow> windowByType(
      final DataStream<BillableEvent> billableEvents) {

    return billableEvents
        .keyBy(event -> event.getType().toString())
        .window(TumblingEventTimeWindows.of(ONE_MONTH))
        .allowedLateness(Time.of(3, TimeUnit.DAYS));
  }

  private WindowedStream<BillableEvent, String, TimeWindow> windowByCustomer(
      DataStream<BillableEvent> billableEvents) {
    return billableEvents
        .keyBy(BillableEvent::getCustomer)
        .window(TumblingEventTimeWindows.of(ONE_MONTH))
        .allowedLateness(Time.of(3, TimeUnit.DAYS));
  }

  private void exposeQueryableTypePreviews(final DataStream<BillableEvent> billableEvents) {
    windowByType(billableEvents)
        .trigger(CountTrigger.of(1))
        .fold(
            Money.zero(EUR),
            (accumulator, value) -> accumulator.plus(value.getAmount()),
            new MonthlySubTotalPreviewFunction(Optional.of(PER_EVENT_TYPE_STATE_NAME)))
        .name("Individual Sums for each EventType per Payment Period");
  }

  private DataStream<MonthlySubtotalByCategory> sumUp(
      WindowedStream<BillableEvent, String, TimeWindow> windowed, Optional<String> stateName) {
    return windowed
        .fold(
            Money.zero(EUR),
            (accumulator, value) -> accumulator.plus(value.getAmount()),
            new MonthlySubTotalPreviewFunction(stateName))
        .name("Individual Sums for each Customer per Payment Period");
  }
}
