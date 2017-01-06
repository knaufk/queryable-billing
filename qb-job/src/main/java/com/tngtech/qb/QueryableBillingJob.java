package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import com.jgrier.flinkstuff.sources.TimestampSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
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
            new FlatMapFunction<BillableEvent, BillableEvent>() {
              List<String> customers = Lists.newArrayList("Anton", "Berta", "Charlie");

              @Override
              public void flatMap(BillableEvent value, Collector<BillableEvent> out)
                  throws Exception {
                customers.forEach(
                    c -> out.collect(value.withNewAmount(nextRandomAmount()).withCustomer(c)));
              }
            })
        .keyBy("customer")
        .flatMap(new QueryableWindow());
    env.execute("Queryable Billing Job");
  }

  @VisibleForTesting
  DataStreamSource<BillableEvent> getSource() {
    return env.addSource(new TimestampSource(100, 1));
  }

  private static double nextRandomAmount() {
    return new Random().nextDouble() * 100;
  }
}
