package com.tngtech.qb.dto;

import com.tngtech.qb.Constants;
import com.tngtech.qb.MonthlySubtotalByCategory;
import lombok.Data;

import java.text.SimpleDateFormat;

@Data
public class MonthlyEventTypeSubTotalResponse {
  private final String type;
  private final String month;
  private final String totalEur;

  public MonthlyEventTypeSubTotalResponse(MonthlySubtotalByCategory subTotal) {
    type = subTotal.getCategory();
    month =
        new SimpleDateFormat(Constants.YEAR_MONTH_PATTERN)
            .format(Long.parseLong(subTotal.getMonth()));
    totalEur = subTotal.getTotal().getAmount().toString();
  }
}
