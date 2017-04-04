package com.tngtech.qb.dto;

import com.tngtech.qb.MonthlySubtotalByCategory;

import java.text.SimpleDateFormat;

public class MonthlyCustomerSubTotalResponse {
  private final String customer;
  private final String month;
  private final String totalEur;

  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM");

  public MonthlyCustomerSubTotalResponse(
      final String customer, final String month, final String totalEur) {
    this.customer = customer;
    this.month = month;
    this.totalEur = totalEur;
  }

  public MonthlyCustomerSubTotalResponse(MonthlySubtotalByCategory subTotal) {
    customer = subTotal.getCategory();
    month = dateFormatter.format(Long.parseLong(subTotal.getMonth()));
    totalEur = subTotal.getTotal().getAmount().toString();
  }

  public String getCustomer() {
    return customer;
  }

  public String getMonth() {
    return month;
  }

  public String getTotalEur() {
    return totalEur;
  }
}
