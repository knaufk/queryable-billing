package com.tngtech.qb;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.util.Objects;
import java.util.List;

public class BillableEvent {

  private long timestampMs;
  private String customer;
  private Money amount;
  private BillableEventType type;

  public BillableEvent() {
    this.timestampMs = 0;
    this.customer = "";
    this.amount = Money.zero(CurrencyUnit.EUR);
  }

  public BillableEvent(
      final long timestampMs,
      final String customer,
      final Money amount,
      final BillableEventType type) {
    this.timestampMs = timestampMs;
    this.customer = customer;
    this.amount = amount;
    this.type = type;
  }

  BillableEvent withEuroAmount(double amountInEuro) {
    return new BillableEvent(
        timestampMs, customer, Money.of(CurrencyUnit.EUR, amountInEuro, RoundingMode.UP), type);
  }

  BillableEvent withCustomer(String newCustomer) {
    return new BillableEvent(timestampMs, newCustomer, amount, type);
  }

  BillableEvent withEventType(BillableEventType type) {
    return new BillableEvent(timestampMs, customer, amount, type);
  }

  public BillableEventType getType() {
    return type;
  }

  public long getTimestampMs() {
    return timestampMs;
  }

  public String getCustomer() {
    return customer;
  }

  public Money getAmount() {
    return amount;
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

  public enum BillableEventType {
    MESSAGE,
    DATA,
    CALL,
    PACK,
    MISC;

    public static List<BillableEventType> asList() {
      return Lists.newArrayList(values());
    }

    public static int count() {
      return values().length;
    }
  }
}
