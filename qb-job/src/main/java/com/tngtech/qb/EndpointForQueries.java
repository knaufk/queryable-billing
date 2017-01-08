package com.tngtech.qb;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class EndpointForQueries extends RichFlatMapFunction<BillableEvent, Object> {
  private final ValueStateDescriptor<BillableEvent> stateDescriptor =
      new ValueStateDescriptor<>(
          Constants.LATEST_EVENT_STATE_NAME, BillableEvent.class, BillableEvent.empty());
  private ValueState<BillableEvent> state;

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    stateDescriptor.setQueryable(Constants.LATEST_EVENT_STATE_NAME);
    state = getRuntimeContext().getState(stateDescriptor);
  }

  @Override
  public void flatMap(BillableEvent value, Collector<Object> out) throws Exception {
    state.update(value);
  }
}
