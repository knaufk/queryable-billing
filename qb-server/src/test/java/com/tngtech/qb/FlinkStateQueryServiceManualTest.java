package com.tngtech.qb;

import com.jgrier.flinkstuff.data.KeyedDataPoint;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FlinkStateQueryServiceManualTest {

  public static void main(String[] args) throws Exception {
    final FlinkStateQueryService queryService =
        new FlinkStateQueryService(
            "22b16e6733dfb49803c8ce1502a0c3e8",
            "/Users/max/github/flink/flink-dist/src/main/resources");
    while (true) {
      SECONDS.sleep(1);
      final List<KeyedDataPoint<Long>> results = queryService.query("Anton");
      for (KeyedDataPoint<Long> result : results) {
        System.out.println(result);
      }
    }
  }
}
