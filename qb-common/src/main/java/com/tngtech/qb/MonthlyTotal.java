package com.tngtech.qb;

import com.google.common.base.MoreObjects;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.Objects;

public class MonthlyTotal {

  private final String customer;
  private final String month;
  private final Money total;

  public MonthlyTotal empty() {
    return new MonthlyTotal("", "", Money.zero(CurrencyUnit.EUR));
  }

  public MonthlyTotal(final String customer, String month, final Money total) {
    this.customer = customer;
    this.month = month;
    this.total = total;
  }

  public String getCustomer() {
    return customer;
  }

  public Money getTotal() {
    return total;
  }

  public String getMonth() {
    return month;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(month);
    sb.append(":\t");
    sb.append(customer);
    sb.append(": ");
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
    final MonthlyTotal that = (MonthlyTotal) o;
    return Objects.equals(customer, that.customer)
        && Objects.equals(month, that.month)
        && Objects.equals(total, that.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customer, month, total);
  }
}
