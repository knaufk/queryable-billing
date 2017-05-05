package com.tngtech.qb;

import lombok.Data;
import org.joda.money.Money;

import static java.math.RoundingMode.UP;
import static org.joda.money.CurrencyUnit.EUR;

@Data
public class BillableEvent {

  private final long timestampMs;
  private final String customer;
  private final Money amount;
  private final BillableEventType type;

  static BillableEvent fromEvent(String eventString) {
    final String[] fields = eventString.split(",");
    return new BillableEvent(
        Long.parseLong(fields[0]),
        fields[1],
        Money.of(EUR, Double.valueOf(fields[2]), UP),
        BillableEventType.valueOf(fields[3].toUpperCase()));
  }

  public enum BillableEventType {
    MESSAGE,
    DATA,
    CALL,
    PACK,
    MISC;
  }
}
