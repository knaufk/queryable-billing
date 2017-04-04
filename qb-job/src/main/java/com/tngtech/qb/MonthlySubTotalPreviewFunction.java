package com.tngtech.qb;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.joda.money.Money;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

class MonthlySubTotalPreviewFunction
    extends RichWindowFunction<Money, MonthlySubtotalByCategory, String, TimeWindow> {

  private final boolean queryable;

  private String stateName;
  private ValueStateDescriptor stateDescriptor;
  private ValueState<MonthlySubtotalByCategory> state;

  MonthlySubTotalPreviewFunction(Optional<String> stateName) {
    queryable = stateName.isPresent();
    if (queryable) {
      this.stateName = stateName.get();
      stateDescriptor = new ValueStateDescriptor<>(this.stateName, MonthlySubtotalByCategory.class);
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
      final Collector<MonthlySubtotalByCategory> out)
      throws Exception {
    Money amount = input.iterator().next();
    String month = String.valueOf(window.getStart() + TimeUnit.DAYS.toMillis(15));

    MonthlySubtotalByCategory monthlySubtotal =
        new MonthlySubtotalByCategory(customer, month, amount);
    if (queryable) {
      state.update(monthlySubtotal);
    }
    out.collect(monthlySubtotal);
  }
}
