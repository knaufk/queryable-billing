package com.tngtech.qb;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.Objects;

public class MonthlySubTotal<T> {

  private final T customer;
  private final String month;
  private final Money total;

  public static MonthlySubTotal empty() {
    return new MonthlySubTotal("", "", Money.zero(CurrencyUnit.EUR));
  }

  public MonthlySubTotal(final T customer, String month, final Money total) {
    this.customer = customer;
    this.month = month;
    this.total = total;
  }

  public T getCustomer() {
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
    final MonthlySubTotal that = (MonthlySubTotal) o;
    return Objects.equals(customer, that.customer)
        && Objects.equals(month, that.month)
        && Objects.equals(total, that.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customer, month, total);
  }
}
