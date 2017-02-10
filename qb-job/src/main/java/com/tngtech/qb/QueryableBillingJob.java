package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import com.tngtech.qb.BillableEvent.BillableEventType;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
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

import static com.tngtech.qb.Constants.PER_EVENT_TYPE_STATE_NAME;

public class QueryableBillingJob {
  private final StreamExecutionEnvironment env;
  private final ParameterTool parameters;

  private static final Time ONE_MONTH = Time.seconds(60);

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
    billableEvents().print();
    process(billableEvents());
    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStream<BillableEvent> billableEvents() {
    return env.addSource(new SimpleBillableEventSource()).name("BillableEventSource");
  }

  private void process(DataStream<BillableEvent> billableEvents) {

    WindowedStream<BillableEvent, String, TimeWindow> perCustomerWindows =
        windowByCustomer(billableEvents);

    WindowedStream<BillableEvent, BillableEventType, TimeWindow> perEventTypeWindows =
        windowByType(billableEvents);

    exposeQueryableTypePreviews(perEventTypeWindows);

    exposeQueryableCustomerPreviews(perCustomerWindows);
    outputFinalInvoice(perCustomerWindows);
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

  private void exposeQueryableCustomerPreviews(
      WindowedStream<BillableEvent, String, TimeWindow> windowed) {
    final WindowedStream<BillableEvent, String, TimeWindow> eventsSoFar =
        windowed.trigger(CountTrigger.of(1));
    sumUp(eventsSoFar, Constants.PER_CUSTOMER_STATE_NAME);
  }

  private void exposeQueryableTypePreviews(
      final WindowedStream<BillableEvent, BillableEventType, TimeWindow> windowed) {
    windowed
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

  private void outputFinalInvoice(WindowedStream<BillableEvent, String, TimeWindow> monthlyEvents) {
    sumUp(monthlyEvents, "something")
        .addSink(
            new BucketingSink<MonthlyCustomerSubTotal>(parameters.getRequired("output"))
                .setInactiveBucketCheckInterval(10)
                .setInactiveBucketThreshold(10))
        .name("Create Final Invoices");
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

  private static class CustomerSubTotalPreviewFunction
      extends RichWindowFunction<Money, MonthlyCustomerSubTotal, String, TimeWindow> {

    private String stateName;

    CustomerSubTotalPreviewFunction(String stateName) {
      this.stateName = stateName;
      stateDescriptor =
          new ValueStateDescriptor<>(
              stateName, MonthlyCustomerSubTotal.class, MonthlyCustomerSubTotal.empty());
    }

    private final ValueStateDescriptor stateDescriptor;
    private ValueState<MonthlyCustomerSubTotal> state;

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
        final Collector<MonthlyCustomerSubTotal> out)
        throws Exception {
      Money amount = input.iterator().next();
      MonthlyCustomerSubTotal monthlySubtotal =
          new MonthlyCustomerSubTotal(
              customer, window.getStart() + " - " + window.getEnd(), amount);
      state.update(monthlySubtotal);
      out.collect(monthlySubtotal);
    }
  }

  private static class EventTypeSubTotalPreviewFunction
      extends RichWindowFunction<Money, MonthlyEventTypeSubTotal, BillableEventType, TimeWindow> {

    private String stateName;

    EventTypeSubTotalPreviewFunction(String stateName) {
      this.stateName = stateName;
      stateDescriptor =
          new ValueStateDescriptor<>(
              stateName, MonthlyEventTypeSubTotal.class, MonthlyEventTypeSubTotal.empty());
    }

    private final ValueStateDescriptor stateDescriptor;
    private ValueState<MonthlyEventTypeSubTotal> state;

    @Override
    public void open(Configuration parameters) throws Exception {
      super.open(parameters);
      stateDescriptor.setQueryable(stateName);
      state = getRuntimeContext().getState(stateDescriptor);
    }

    @Override
    public void apply(
        final BillableEventType type,
        final TimeWindow window,
        final Iterable<Money> input,
        final Collector<MonthlyEventTypeSubTotal> out)
        throws Exception {
      Money amount = input.iterator().next();
      MonthlyEventTypeSubTotal monthlySubtotal =
          new MonthlyEventTypeSubTotal(type, window.getStart() + " - " + window.getEnd(), amount);
      state.update(monthlySubtotal);
      out.collect(monthlySubtotal);
    }
  }
}
