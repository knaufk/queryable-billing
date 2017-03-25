package com.tngtech.qb;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.joda.money.Money;

import java.util.Optional;

class CustomerSubTotalPreviewFunction
    extends RichWindowFunction<Money, MonthlyCustomerSubTotal, String, TimeWindow> {

  private final boolean queryable;

  private String stateName;
  private ValueStateDescriptor stateDescriptor;
  private ValueState<MonthlyCustomerSubTotal> state;

  CustomerSubTotalPreviewFunction(Optional<String> stateName) {
    queryable = stateName.isPresent();
    if (queryable) {
      this.stateName = stateName.get();
      stateDescriptor = new ValueStateDescriptor<>(this.stateName, MonthlyCustomerSubTotal.class);
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    if (queryable) {
      stateDescriptor.setQueryable(stateName);
      state = getRuntimeContext().getState(stateDescriptor);
    }
  }

  @Override
  public void apply(
      final String customer,
      final TimeWindow window,
      final Iterable<Money> input,
      final Collector<MonthlyCustomerSubTotal> out)
      throws Exception {
    Money amount = input.iterator().next();
    String month = String.valueOf(window.getStart());

    MonthlyCustomerSubTotal monthlySubtotal = new MonthlyCustomerSubTotal(customer, month, amount);
    if (queryable) {
      state.update(monthlySubtotal);
    }
    out.collect(monthlySubtotal);
  }
}
