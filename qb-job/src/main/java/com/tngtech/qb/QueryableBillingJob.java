package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import com.jgrier.flinkstuff.data.DataPoint;
import com.jgrier.flinkstuff.data.KeyedDataPoint;
import com.jgrier.flinkstuff.sources.TimestampSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Random;

class QueryableBillingJob {

  @VisibleForTesting
  final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

  void run() throws Exception {
    getSource()
        .flatMap(
            new FlatMapFunction<DataPoint<Long>, KeyedDataPoint<Long>>() {
              List<String> customers = Lists.newArrayList("Anton", "Berta", "Charlie");

              @Override
              public void flatMap(DataPoint<Long> value, Collector<KeyedDataPoint<Long>> out)
                  throws Exception {
                customers.forEach(c -> out.collect(value.withNewValue(nextGaussian()).withKey(c)));
              }
            })
        .keyBy(
            new KeySelector<KeyedDataPoint<Long>, String>() {
              @Override
              public String getKey(KeyedDataPoint<Long> value) throws Exception {
                return value.getKey();
              }
            })
        .flatMap(new QueryableWindow());
    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStreamSource<DataPoint<Long>> getSource() {
    return env.addSource(new TimestampSource(100, 1));
  }

  private static long nextGaussian() {
    return Math.round(new Random().nextGaussian() * 100);
  }
}
