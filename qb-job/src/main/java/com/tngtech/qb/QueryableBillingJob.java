package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import com.jgrier.flinkstuff.sources.TimestampSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink;
import org.apache.flink.util.Collector;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class QueryableBillingJob {
  @VisibleForTesting
  final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

  private final ParameterTool parameters;

  QueryableBillingJob(ParameterTool parameters) {
    this.parameters = parameters;
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
  }

  public static void main(String[] args) throws Exception {
    ParameterTool parameters = ParameterTool.fromArgs(args);
    new QueryableBillingJob(parameters).run();
  }

  void run() throws Exception {
    invoice(readBillableEvents());
    env.execute("Queryable Billing Job");
  }

  private SingleOutputStreamOperator<BillableEvent> readBillableEvents() {
    return getSource()
        .flatMap(
            new FlatMapFunction<BillableEvent, BillableEvent>() {

              @Override
              public void flatMap(BillableEvent value, Collector<BillableEvent> out)
                  throws Exception {
                Lists.newArrayList("Anton", "Berta", "Charlie")
                    .forEach(
                        c -> out.collect(value.withNewAmount(Math.random() * 100).withCustomer(c)));
              }
            })
        .name("Random Test Data");
  }

  @VisibleForTesting
  DataStream<BillableEvent> getSource() {
    return env.addSource(new TimestampSource(100, 1)).name("Timestamp Source");
  }

  private void invoice(SingleOutputStreamOperator<BillableEvent> billableEvents) {
    makeCustomersQueryable(billableEvents);

    sumQueryablePreviews(window(billableEvents));
    outputFinalInvoice(window(billableEvents));
  }

  private WindowedStream<BillableEvent, Tuple, TimeWindow> window(
      SingleOutputStreamOperator<BillableEvent> billableEvents) {
    return billableEvents.keyBy("customer").window(TumblingEventTimeWindows.of(Time.seconds(10)));
  }

  private SingleOutputStreamOperator<Object> makeCustomersQueryable(
      SingleOutputStreamOperator<BillableEvent> billableEvents) {
    return billableEvents
        .keyBy((KeySelector<BillableEvent, Integer>) value -> Constants.CUSTOMERS_KEY)
        .flatMap(new EndpointForCustomerQueries())
        .name("Add Customers to Queryable State");
  }

  private void sumQueryablePreviews(WindowedStream<BillableEvent, Tuple, TimeWindow> windowed) {
    final WindowedStream<BillableEvent, Tuple, TimeWindow> alwaysTriggering =
        windowed.trigger(CountTrigger.of(1));
    final SingleOutputStreamOperator<BillableEvent> summedPreviews = tallyUp(alwaysTriggering);

    summedPreviews
        .keyBy("customer")
        .flatMap(new EndpointForQueries())
        .name("Add Summed Previes to Queryable State");
  }

  private void outputFinalInvoice(WindowedStream<BillableEvent, Tuple, TimeWindow> windowed) {
    tallyUp(windowed)
        .addSink(
            new BucketingSink<BillableEvent>(parameters.getRequired("output"))
                .setInactiveBucketCheckInterval(10)
                .setInactiveBucketThreshold(10))
        .name("Create Final Invoices");
  }

  private SingleOutputStreamOperator<BillableEvent> tallyUp(
      WindowedStream<BillableEvent, Tuple, TimeWindow> windowed) {
    return windowed
        .fold(
            new Tuple2<>("", Money.zero(CurrencyUnit.EUR)),
            (FoldFunction<BillableEvent, Tuple2<String, Money>>)
                (accumulator, value) ->
                    new Tuple2<>(value.getCustomer(), value.getAmount().plus(accumulator.f1)),
            (WindowFunction<Tuple2<String, Money>, BillableEvent, Tuple, TimeWindow>)
                (tuple, window, input, out) -> {
                  final Tuple2<String, Money> customerWithAmount = input.iterator().next();
                  out.collect(
                      new BillableEvent(
                          window.getStart(), customerWithAmount.f0, customerWithAmount.f1));
                },
            TypeInformation.of(new TypeHint<Tuple2<String, Money>>() {}),
            TypeInformation.of(new TypeHint<BillableEvent>() {}))
        .name("Individual Sums for each Customer per Payment Period");
  }
}
