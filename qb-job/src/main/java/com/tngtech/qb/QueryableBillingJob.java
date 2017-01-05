package com.tngtech.qb;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

class QueryableBillingJob {

  private final StreamExecutionEnvironment env =
      StreamExecutionEnvironment.getExecutionEnvironment();

  void run() throws Exception {
    env.fromElements(1, 2, 3).print();
    env.execute();
  }
}
