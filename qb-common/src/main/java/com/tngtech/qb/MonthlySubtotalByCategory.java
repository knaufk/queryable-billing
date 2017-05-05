package com.tngtech.qb;

import lombok.Data;
import org.joda.money.Money;

import java.text.SimpleDateFormat;

@Data
public class MonthlySubtotalByCategory {

  private final String category;
  private final String month;
  private final Money total;

  private final SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.YEAR_MONTH_PATTERN);

  String getFormattedMonth() {
    return dateFormatter.format(Long.parseLong(month));
  }

  @Override
  public String toString() {
    return getFormattedMonth() + "\t" + category + "\t " + total;
  }
}
