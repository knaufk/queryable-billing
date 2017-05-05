package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import org.apache.flink.api.java.functions.KeySelector;
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

import java.util.Properties;

import static com.tngtech.qb.Constants.PER_CUSTOMER_STATE_NAME;
import static com.tngtech.qb.Constants.PER_EVENT_TYPE_STATE_NAME;
import static java.util.concurrent.TimeUnit.*;
import static org.joda.money.CurrencyUnit.*;

public class QueryableBillingJob {

  private static final Time ALLOWED_LATENESS = Time.of(3, DAYS);
  private static final Time MAX_OUT_OF_ORDERNESS = Time.of(12, HOURS);

  private static final int INACTIVE_BUCKET_CHECK_INTERVAL_MS = 100;
  private static final int INACTIVE_BUCKET_THRESHOLD_MS = 1_000;
  private static final int CHECKPOINT_INTERVAL_MS = 10_000;

  private static final Time PAYMENT_PERIOD = Time.days(30);
  private static final String GROUP_ID = "BillingJob";

  private final StreamExecutionEnvironment env;
  private final ParameterTool parameters;

  @VisibleForTesting
  QueryableBillingJob(final StreamExecutionEnvironment env, ParameterTool parameters) {
    this.parameters = parameters;
    this.env = env;
    this.env.enableCheckpointing(CHECKPOINT_INTERVAL_MS);
    this.env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
  }

  public static void main(String[] args) throws Exception {
    ParameterTool parameters = ParameterTool.fromArgs(args);
    new QueryableBillingJob(StreamExecutionEnvironment.getExecutionEnvironment(), parameters).run();
  }

  @VisibleForTesting
  void run() throws Exception {
    DataStream<BillableEvent> billableEvents = billableEvents();

    outputFinalInvoice(billableEvents);

    exposeQueryableCustomerPreviews(billableEvents);
    exposeQueryableTypePreviews(billableEvents);

    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStream<BillableEvent> billableEvents() {

    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", parameters.getRequired("bootstrap-servers"));
    properties.setProperty("group.id", GROUP_ID);
    properties.setProperty("auto.offset.reset", "earliest");

    FlinkKafkaConsumer010<String> kafkaSource =
        new FlinkKafkaConsumer010<>(
            Constants.SRC_KAFKA_TOPIC, new SimpleStringSchema(), properties);
    kafkaSource.assignTimestampsAndWatermarks(
        new RawBillableEventTimestampExtractor(MAX_OUT_OF_ORDERNESS));

    return env.addSource(kafkaSource)
        .name("BillableEventSource (Kafka)")
        .map(BillableEvent::fromEvent);
  }

  private void outputFinalInvoice(final DataStream<BillableEvent> billableEvents) {
    sumUp(monthlyWindows(billableEvents, BillableEvent::getCustomer), "Customer", false)
        .addSink(
            new BucketingSink<MonthlySubtotalByCategory>(parameters.getRequired("output"))
                .setBucketer(new MonthBucketer())
                .setInactiveBucketCheckInterval(INACTIVE_BUCKET_CHECK_INTERVAL_MS)
                .setInactiveBucketThreshold(INACTIVE_BUCKET_THRESHOLD_MS))
        .name("Create Final Invoices");
  }

  private void exposeQueryableCustomerPreviews(final DataStream<BillableEvent> billableEvents) {
    final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar =
        monthlyWindows(billableEvents, BillableEvent::getCustomer).trigger(CountTrigger.of(1));
    sumUpQueryably(eventsSoFar, PER_CUSTOMER_STATE_NAME);
  }

  private void exposeQueryableTypePreviews(final DataStream<BillableEvent> billableEvents) {
    final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar =
        monthlyWindows(billableEvents, event -> event.getType().toString())
            .trigger(CountTrigger.of(1));
    sumUpQueryably(eventsSoFar, PER_EVENT_TYPE_STATE_NAME);
  }

  private void sumUpQueryably(
      final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar, final String stateName) {
    sumUp(eventsSoFar, stateName, true);
  }

  private WindowedStream<BillableEvent, String, TimeWindow> monthlyWindows(
      DataStream<BillableEvent> billableEvents, KeySelector<BillableEvent, String> keySelector) {
    return billableEvents
        .keyBy(keySelector)
        .window(TumblingEventTimeWindows.of(PAYMENT_PERIOD))
        .allowedLateness(ALLOWED_LATENESS);
  }

  private DataStream<MonthlySubtotalByCategory> sumUp(
      WindowedStream<BillableEvent, String, TimeWindow> windowed,
      String categoryName,
      final boolean queryable) {
    final MonthlySubTotalPreviewFunction windowFunction = new MonthlySubTotalPreviewFunction();
    if (queryable) {
      windowFunction.makeStateQueryable(categoryName);
    }
    return windowed
        .fold(
            Money.zero(EUR),
            (accumulator, value) -> accumulator.plus(value.getAmount()),
            windowFunction)
        .name("Individual Sums for each " + categoryName + " per Payment Period");
  }

  private static class RawBillableEventTimestampExtractor
      extends BoundedOutOfOrdernessTimestampExtractor<String> {

    private RawBillableEventTimestampExtractor(Time maxOutOfOrderness) {
      super(maxOutOfOrderness);
    }

    @Override
    public long extractTimestamp(final String element) {
      return Long.parseLong(element.substring(0, element.indexOf(',')));
    }
  }
}
