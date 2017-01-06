package com.tngtech.qb;

import com.google.common.base.MoreObjects;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.util.Objects;

public class BillableEvent {
  private long timestampMs;
  private String customer;
  private Money amount;

  public BillableEvent() {
    this.timestampMs = 0;
    this.customer = "";
    this.amount = Money.of(CurrencyUnit.EUR, 0);
  }

  public BillableEvent(long timestampMs, String customer, Money amount) {
    this.timestampMs = timestampMs;
    this.customer = customer;
    this.amount = amount;
  }

  BillableEvent withNewAmount(double amountInEuro) {
    return new BillableEvent(
        timestampMs, customer, Money.of(CurrencyUnit.EUR, amountInEuro, RoundingMode.UP));
  }

  BillableEvent withCustomer(String newCustomer) {
    return new BillableEvent(timestampMs, newCustomer, amount);
  }

  public long getTimestampMs() {
    return timestampMs;
  }

  public void setTimestampMs(long timestampMs) {
    this.timestampMs = timestampMs;
  }

  public String getCustomer() {
    return customer;
  }

  public void setCustomer(String customer) {
    this.customer = customer;
  }

  public Money getAmount() {
    return amount;
  }

  public void setAmount(Money amount) {
    this.amount = amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BillableEvent that = (BillableEvent) o;
    return timestampMs == that.timestampMs
        && Objects.equals(customer, that.customer)
        && Objects.equals(amount, that.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestampMs, customer, amount);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("timestampMs", timestampMs)
        .add("customer", customer)
        .add("amount", amount)
        .toString();
  }
}
