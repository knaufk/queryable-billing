package com.tngtech.qb;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.joda.money.Money;

class EventTypeSubTotalPreviewFunction
    extends RichWindowFunction<
        Money, MonthlyEventTypeSubTotal, BillableEvent.BillableEventType, TimeWindow> {

  private String stateName;

  EventTypeSubTotalPreviewFunction(String stateName) {
    this.stateName = stateName;
    stateDescriptor = new ValueStateDescriptor<>(stateName, MonthlyEventTypeSubTotal.class);
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
      final BillableEvent.BillableEventType type,
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
