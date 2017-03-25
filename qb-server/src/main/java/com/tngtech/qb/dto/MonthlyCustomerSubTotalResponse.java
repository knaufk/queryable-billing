package com.tngtech.qb.dto;

import com.tngtech.qb.MonthlyCustomerSubTotal;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

public class MonthlyCustomerSubTotalResponse {
  private final String customer;
  private final String month;
  private final String totalEur;

  SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM yy");

  public MonthlyCustomerSubTotalResponse(
      final String customer, final String month, final String totalEur) {
    this.customer = customer;
    this.month = month;
    this.totalEur = totalEur;
  }

  public MonthlyCustomerSubTotalResponse(MonthlyCustomerSubTotal subTotal) {
    customer = subTotal.getBasis();
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
