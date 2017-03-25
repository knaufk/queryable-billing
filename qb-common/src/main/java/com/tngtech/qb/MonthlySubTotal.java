package com.tngtech.qb;

import org.joda.money.Money;

import java.util.Objects;

public class MonthlySubTotal<T> {

  private final T basis;
  private final String month;
  private final Money total;

  public MonthlySubTotal(final T basis, String month, final Money total) {
    this.basis = basis;
    this.month = month;
    this.total = total;
  }

  public T getBasis() {
    return basis;
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
    sb.append("\t");
    sb.append(basis);
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
    final MonthlySubTotal that = (MonthlySubTotal) o;
    return Objects.equals(basis, that.basis)
        && Objects.equals(month, that.month)
        && Objects.equals(total, that.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(basis, month, total);
  }
}
