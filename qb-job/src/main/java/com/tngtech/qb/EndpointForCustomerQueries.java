package com.tngtech.qb;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.FoldingState;
import org.apache.flink.api.common.state.FoldingStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EndpointForCustomerQueries extends RichFlatMapFunction<BillableEvent, Object> {
  private final FoldingStateDescriptor<BillableEvent, Set<String>> foldingStateDescriptor;
  private FoldingState<BillableEvent, Set<String>> customersState;

  EndpointForCustomerQueries() {
    foldingStateDescriptor =
        new FoldingStateDescriptor<BillableEvent, Set<String>>(
            Constants.CUSTOMERS_STATE_NAME,
            Collections.emptySet(),
            new FoldFunction<BillableEvent, Set<String>>() {
              @Override
              public Set<String> fold(Set<String> accumulator, BillableEvent value)
                  throws Exception {
                final HashSet<String> customers = new HashSet<>(accumulator);
                customers.add(value.getCustomer());
                return customers;
              }
            },
            TypeInformation.of(new TypeHint<Set<String>>() {}));
    foldingStateDescriptor.setQueryable(Constants.CUSTOMERS_STATE_NAME);
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    customersState = getRuntimeContext().getFoldingState(foldingStateDescriptor);
  }

  @Override
  public void flatMap(BillableEvent value, Collector<Object> out) throws Exception {
    customersState.add(value);
  }
}
