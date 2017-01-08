package com.tngtech.qb;

public class FlinkStateQueryServiceManualTest {
  public static void main(String[] args) throws Exception {
    final FlinkStateQueryService queryService =
        new FlinkStateQueryService(
            "af86615d542185e90a2dce80ae1e521f",
            "/Users/max/github/flink/flink-dist/src/main/resources");
    while (true) {
      queryService
          .findAllCustomers()
          .forEach(
              c -> {
                try {
                  System.out.println(queryService.findOne(c));
                } catch (Exception e) {
                  e.printStackTrace();
                }
              });
    }
  }
}
