package com.tngtech.qb;

public class FlinkStateQueryServiceManualTest {
  public static void main(String[] args) throws Exception {
    final FlinkStateQueryService queryService =
        new FlinkStateQueryService(
            "e9d622a7eceb115cea981def7ca0422c",
            "/Users/bodem/github/flink/flink-dist/src/main/resources");
    while (true) {
      final MonthlyCustomerSubTotal anton = queryService.findOne("Mike");
      System.out.println(anton);
      //            ((MonthlyCustomerSubTotal) anton)
      ////                    .findAllCustomers()
      //                    .forEach(
      //                            c -> {
      //                                try {
      ////                                    System.out.println(queryService.findOne(c));
      //                                    System.out.println(c);
      //                                } catch (Exception e) {
      //                                    e.printStackTrace();
      //                                }
      //                            });
    }
  }
}
