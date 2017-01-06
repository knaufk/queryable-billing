package com.tngtech.qb;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FlinkStateQueryServiceManualTest {

  public static void main(String[] args) throws Exception {
    final FlinkStateQueryService queryService =
        new FlinkStateQueryService(
            "22b16e6733dfb49803c8ce1502a0c3e8",
            "/Users/max/github/flink/flink-dist/src/main/resources");
    while (true) {
      SECONDS.sleep(1);
      for (BillableEvent result : queryService.query("Anton")) {
        System.out.println(result);
      }
    }
  }
}
