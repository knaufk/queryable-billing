package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink;
import org.apache.flink.util.Collector;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.concurrent.TimeUnit;

public class QueryableBillingJob {
  private final StreamExecutionEnvironment env;
  private final ParameterTool parameters;

  QueryableBillingJob(
      final StreamExecutionEnvironment executionEnvironment, ParameterTool parameters) {
    this.parameters = parameters;
    this.env = executionEnvironment;
    this.env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.enableCheckpointing(100);
  }

  public static void main(String[] args) throws Exception {
    ParameterTool parameters = ParameterTool.fromArgs(args);
    new QueryableBillingJob(StreamExecutionEnvironment.getExecutionEnvironment(), parameters).run();
  }

  void run() throws Exception {
    process(billableEvents());
    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStream<BillableEvent> billableEvents() {
    return env.addSource(new SimpleBillableEventSource()).name("BillableEventSource");
  }

  private void process(DataStream<BillableEvent> billableEvents) {
    exposeQueryablePreviews(window(billableEvents));
    outputFinalInvoice(window(billableEvents));
  }

  private WindowedStream<BillableEvent, String, TimeWindow> window(
      DataStream<BillableEvent> billableEvents) {
    return billableEvents
        .keyBy((KeySelector<BillableEvent, String>) value -> value.getCustomer())
        .window(TumblingEventTimeWindows.of(Time.seconds(60)))
        .allowedLateness(Time.of(2, TimeUnit.SECONDS));
  }

  private void exposeQueryablePreviews(WindowedStream<BillableEvent, String, TimeWindow> windowed) {
    final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar =
        windowed.trigger(CountTrigger.of(1));
    sumUp(eventsSoFar, Constants.LATEST_EVENT_STATE_NAME);
  }

  private void outputFinalInvoice(WindowedStream<BillableEvent, String, TimeWindow> monthlyEvents) {
    sumUp(monthlyEvents, "something")
        .addSink(
            new BucketingSink<MonthlyTotal>(parameters.getRequired("output"))
                .setInactiveBucketCheckInterval(10)
                .setInactiveBucketThreshold(10))
        .name("Create Final Invoices");
  }

  private DataStream<MonthlyTotal> sumUp(
      WindowedStream<BillableEvent, String, TimeWindow> windowed, String stateName) {
    return windowed
        .fold(
            Money.zero(CurrencyUnit.EUR),
            (accumulator, value) -> accumulator.plus(value.getAmount()),
            new QueryableWindowFunction(stateName))
        .name("Individual Sums for each Customer per Payment Period");
  }

  private static class QueryableWindowFunction
      extends RichWindowFunction<Money, MonthlyTotal, String, TimeWindow> {

    private String stateName;

    public QueryableWindowFunction(String stateName) {
      this.stateName = stateName;
      stateDescriptor = new ValueStateDescriptor<>(stateName, MonthlyTotal.class);
    }

    private final ValueStateDescriptor stateDescriptor;
    private ValueState<MonthlyTotal> state;

    @Override
    public void open(Configuration parameters) throws Exception {
      super.open(parameters);
      stateDescriptor.setQueryable(stateName);
      state = getRuntimeContext().getState(stateDescriptor);
    }

    @Override
    public void apply(
        final String customer,
        final TimeWindow window,
        final Iterable<Money> input,
        final Collector<MonthlyTotal> out)
        throws Exception {
      Money amount = input.iterator().next();
      MonthlyTotal monthlySubtotal =
          new MonthlyTotal(customer, window.getStart() + " - " + window.getEnd(), amount);
      state.update(monthlySubtotal);
      out.collect(monthlySubtotal);
    }
  }
}
