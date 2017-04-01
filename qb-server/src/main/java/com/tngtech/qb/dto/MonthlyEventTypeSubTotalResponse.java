package com.tngtech.qb.dto;

import com.tngtech.qb.BillableEvent.BillableEventType;
import com.tngtech.qb.MonthlyEventTypeSubTotal;

import java.text.SimpleDateFormat;

public class MonthlyEventTypeSubTotalResponse {
  private final BillableEventType type;
  private final String month;
  private final String totalEur;

  SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM yy");
  public MonthlyEventTypeSubTotalResponse(
      final BillableEventType customer, final String month, final String totalEur) {
    this.type = customer;
    this.month = month;
    this.totalEur = totalEur;
  }

  public MonthlyEventTypeSubTotalResponse(MonthlyEventTypeSubTotal subTotal) {
    type = subTotal.getBasis();
    month = dateFormatter.format(Long.parseLong(subTotal.getMonth()));
    totalEur = subTotal.getTotal().getAmount().toString();
  }

  public BillableEventType getType() {
    return type;
  }

  public String getMonth() {
    return month;
  }

  public String getTotalEur() {
    return totalEur;
  }
}
