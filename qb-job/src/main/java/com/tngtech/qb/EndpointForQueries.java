package com.tngtech.qb;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.List;

public class EndpointForQueries extends RichFlatMapFunction<BillableEvent, List<BillableEvent>> {
  private final ReducingStateDescriptor<BillableEvent> reducingStateDescriptor;
  private ReducingState<BillableEvent> stateWithLatestEvent;

  EndpointForQueries() {
    reducingStateDescriptor =
        new ReducingStateDescriptor<BillableEvent>(
            Constants.STATE_NAME,
            new ReduceFunction<BillableEvent>() {
              @Override
              public BillableEvent reduce(BillableEvent value1, BillableEvent value2)
                  throws Exception {
                return value2;
              }
            },
            BillableEvent.class);
    reducingStateDescriptor.setQueryable(Constants.STATE_NAME);
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    stateWithLatestEvent = getRuntimeContext().getReducingState(reducingStateDescriptor);
  }

  @Override
  public void flatMap(BillableEvent value, Collector<List<BillableEvent>> out) throws Exception {
    stateWithLatestEvent.add(value);
  }
}
