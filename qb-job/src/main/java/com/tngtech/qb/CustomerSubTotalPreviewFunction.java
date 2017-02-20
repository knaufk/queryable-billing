package com.tngtech.qb;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.joda.money.Money;

class CustomerSubTotalPreviewFunction
    extends RichWindowFunction<Money, MonthlyCustomerSubTotal, String, TimeWindow> {

  private String stateName;

  CustomerSubTotalPreviewFunction(String stateName) {
    this.stateName = stateName;
    stateDescriptor = new ValueStateDescriptor<>(stateName, MonthlyCustomerSubTotal.class);
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
        new MonthlyCustomerSubTotal(customer, window.getStart() + " - " + window.getEnd(), amount);
    state.update(monthlySubtotal);
    out.collect(monthlySubtotal);
  }
}
