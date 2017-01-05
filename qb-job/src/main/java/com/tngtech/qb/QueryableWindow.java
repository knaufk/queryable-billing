package com.tngtech.qb;

import com.jgrier.flinkstuff.data.KeyedDataPoint;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.List;

public class QueryableWindow
    extends RichFlatMapFunction<KeyedDataPoint<Long>, List<KeyedDataPoint<Long>>> {
  private static final String LIST_STATE_NAME = "time-series";

  private ValueState<Integer> countState;

  private final ListStateDescriptor<KeyedDataPoint<Long>> listStateDescriptor;
  private ListState<KeyedDataPoint<Long>> listState;

  QueryableWindow() {
    listStateDescriptor =
        new ListStateDescriptor<>(
            LIST_STATE_NAME, TypeInformation.of(new TypeHint<KeyedDataPoint<Long>>() {}));
    listStateDescriptor.setQueryable(LIST_STATE_NAME);
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    countState =
        getRuntimeContext()
            .getState(new ValueStateDescriptor<>("time-series-count", Integer.class, 0));
    listState = getRuntimeContext().getListState(listStateDescriptor);
  }

  @Override
  public void flatMap(KeyedDataPoint<Long> value, Collector<List<KeyedDataPoint<Long>>> out)
      throws Exception {
    int count = countState.value();
    if (count == 200) {
      listState.clear();
      count = 0;
    }
    listState.add(value);
    countState.update(count + 1);
  }
}
