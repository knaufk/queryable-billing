package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import com.tngtech.qb.BillableEvent.BillableEventType;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
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
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.tngtech.qb.Constants.PER_EVENT_TYPE_STATE_NAME;

public class QueryableBillingJob {

  private final StreamExecutionEnvironment env;
  private final ParameterTool parameters;

  private static final Time ONE_MONTH = Time.seconds(5);

  QueryableBillingJob(
      final StreamExecutionEnvironment executionEnvironment, ParameterTool parameters) {
    this.parameters = parameters;
    env = executionEnvironment;
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.enableCheckpointing(100);
  }

  public static void main(String[] args) throws Exception {
    ParameterTool parameters = ParameterTool.fromArgs(args);
    new QueryableBillingJob(StreamExecutionEnvironment.getExecutionEnvironment(), parameters).run();
  }

  void run() throws Exception {
    billableEvents().print();
    process(billableEvents());
    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStream<BillableEvent> billableEvents() {
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", parameters.getRequired("bootstrap-servers"));
    properties.setProperty("group.id", "test");
    //TODO serialization (avro?), including event types
    //TODO timestamp in kafka?
    final DataStream<String> strings =
        env.addSource(
                new FlinkKafkaConsumer010<>(
                    Constants.SRC_KAFKA_TOPIC, new SimpleStringSchema(), properties))
            .name("BillableEventSource (Kafka)");
    final SingleOutputStreamOperator<BillableEvent> events =
        strings
            .flatMap(
                (FlatMapFunction<String, BillableEvent>)
                    (value, out) -> {
                      try {
                        final String[] fields = value.split(",");
                        out.collect(
                            new BillableEvent(
                                Long.valueOf(fields[0]),
                                fields[1],
                                Money.of(
                                    CurrencyUnit.EUR, Double.valueOf(fields[2]), RoundingMode.UP),
                                BillableEventType.MISC));
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                    })
            .returns(BillableEvent.class);
    return events.assignTimestampsAndWatermarks(
        new BoundedOutOfOrdernessTimestampExtractor<BillableEvent>(Time.of(1, TimeUnit.SECONDS)) {
          @Override
          public long extractTimestamp(final BillableEvent element) {
            return element.getTimestampMs();
          }
        });
  }

  private void process(DataStream<BillableEvent> billableEvents) {
    outputFinalInvoice(billableEvents);

    exposeQueryableCustomerPreviews(billableEvents);
    //TODO key group problem
    //    exposeQueryableTypePreviews(billableEvents);
  }

  private void outputFinalInvoice(final DataStream<BillableEvent> billableEvents) {
    //TODO why state here?
    sumUp(windowByCustomer(billableEvents), "something")
        .addSink(
            new BucketingSink<MonthlyCustomerSubTotal>(parameters.getRequired("output"))
                .setInactiveBucketCheckInterval(10)
                .setInactiveBucketThreshold(10))
        .name("Create Final Invoices");
  }

  private void exposeQueryableCustomerPreviews(final DataStream<BillableEvent> billableEvents) {
    final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar =
        windowByCustomer(billableEvents).trigger(CountTrigger.of(1));
    sumUp(eventsSoFar, Constants.PER_CUSTOMER_STATE_NAME);
  }

  private WindowedStream<BillableEvent, BillableEventType, TimeWindow> windowByType(
      final DataStream<BillableEvent> billableEvents) {
    return billableEvents
        .keyBy((KeySelector<BillableEvent, BillableEventType>) BillableEvent::getType)
        .window(TumblingEventTimeWindows.of(ONE_MONTH))
        .allowedLateness(Time.of(2, TimeUnit.SECONDS));
  }

  private WindowedStream<BillableEvent, String, TimeWindow> windowByCustomer(
      DataStream<BillableEvent> billableEvents) {
    return billableEvents
        .keyBy((KeySelector<BillableEvent, String>) BillableEvent::getCustomer)
        .window(TumblingEventTimeWindows.of(ONE_MONTH))
        .allowedLateness(Time.of(2, TimeUnit.SECONDS));
  }

  private void exposeQueryableTypePreviews(final DataStream<BillableEvent> billableEvents) {
    windowByType(billableEvents)
        .trigger(CountTrigger.of(1))
        .fold(
            Money.zero(CurrencyUnit.EUR),
            (FoldFunction<BillableEvent, Money>)
                (accumulator, value) -> accumulator.plus(value.getAmount()),
            new EventTypeSubTotalPreviewFunction(PER_EVENT_TYPE_STATE_NAME),
            TypeInformation.of(new TypeHint<Money>() {}),
            TypeInformation.of(new TypeHint<MonthlyEventTypeSubTotal>() {}))
        .name("Individual Sums for each EventType per Payment Period");
  }

  private DataStream<MonthlyCustomerSubTotal> sumUp(
      WindowedStream<BillableEvent, String, TimeWindow> windowed, String stateName) {
    return windowed
        .fold(
            Money.zero(CurrencyUnit.EUR),
            (accumulator, value) -> accumulator.plus(value.getAmount()),
            new CustomerSubTotalPreviewFunction(stateName),
            TypeInformation.of(new TypeHint<Money>() {}),
            TypeInformation.of(new TypeHint<MonthlyCustomerSubTotal>() {}))
        .name("Individual Sums for each Customer per Payment Period");
  }
}
