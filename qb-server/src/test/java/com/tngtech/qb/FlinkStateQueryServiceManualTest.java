package com.tngtech.qb;

public class FlinkStateQueryServiceManualTest {
  public static void main(String[] args) throws Exception {
    final FlinkStateQueryService queryService =
        new FlinkStateQueryService(
            "b7d654e6fe66965266aac33ec10d421d",
            "/Users/max/github/flink/flink-dist/src/main/resources");
    while (true) {
      System.err.println(queryService.query("Anton"));
    }
  }
}
