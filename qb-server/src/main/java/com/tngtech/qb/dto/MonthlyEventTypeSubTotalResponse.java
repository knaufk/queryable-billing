package com.tngtech.qb.dto;

import com.tngtech.qb.MonthlySubtotalByCategory;

import java.text.SimpleDateFormat;

public class MonthlyEventTypeSubTotalResponse {
  private final String type;
  private final String month;
  private final String totalEur;

  SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM yy");

  public MonthlyEventTypeSubTotalResponse(
      final String type, final String month, final String totalEur) {
    this.type = type;
    this.month = month;
    this.totalEur = totalEur;
  }

  public MonthlyEventTypeSubTotalResponse(MonthlySubtotalByCategory subTotal) {
    type = subTotal.getCategory();
    month = dateFormatter.format(Long.parseLong(subTotal.getMonth()));
    totalEur = subTotal.getTotal().getAmount().toString();
  }

  public String getType() {
    return type;
  }

  public String getMonth() {
    return month;
  }

  public String getTotalEur() {
    return totalEur;
  }
}
