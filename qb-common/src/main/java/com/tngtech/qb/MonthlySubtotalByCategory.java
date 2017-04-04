package com.tngtech.qb;

import org.joda.money.Money;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class MonthlySubtotalByCategory {

  private final String category;
  private final String month;
  private final Money total;
  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM");;

  public MonthlySubtotalByCategory(final String category, String month, final Money total) {
    this.category = category;
    this.month = month;
    this.total = total;
  }

  public String getCategory() {
    return category;
  }

  public Money getTotal() {
    return total;
  }

  public String getMonth() {
    return month;
  }

  public String getFormattedMonth() {
    return dateFormatter.format(Long.parseLong(month));
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append(getFormattedMonth());
    sb.append("\t");
    sb.append(category);
    sb.append("\t ");
    sb.append(total);
    return sb.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MonthlySubtotalByCategory that = (MonthlySubtotalByCategory) o;
    return Objects.equals(category, that.category)
        && Objects.equals(month, that.month)
        && Objects.equals(total, that.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, month, total);
  }
}
