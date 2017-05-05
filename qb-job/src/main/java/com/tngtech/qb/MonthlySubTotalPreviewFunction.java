package com.tngtech.qb;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
class MonthlySubTotalPreviewFunction
    extends RichWindowFunction<Money, MonthlySubtotalByCategory, String, TimeWindow> {

  private final boolean queryable;
  private final SimpleDateFormat simpleDateFormat =
      new SimpleDateFormat(Constants.YEAR_MONTH_PATTERN);

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
    long timestamp = window.getStart() + TimeUnit.DAYS.toMillis(15);
    String month = String.valueOf(timestamp);
    if (!queryable) {
      log.info(
          "customer={}, month={}, amount={}", customer, simpleDateFormat.format(timestamp), amount);
    }

    MonthlySubtotalByCategory monthlySubtotal =
        new MonthlySubtotalByCategory(customer, month, amount);
    if (queryable) {
      state.update(monthlySubtotal);
    }
    out.collect(monthlySubtotal);
  }
}
