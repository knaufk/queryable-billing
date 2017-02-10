package com.tngtech.qb;

import com.tngtech.qb.BillableEvent.BillableEventType;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.swing.event.DocumentEvent;

public class MonthlyEventTypeSubTotal extends MonthlySubTotal<BillableEventType> {

  public MonthlyEventTypeSubTotal(
      final BillableEventType type, final String month, final Money total) {
    super(type, month, total);
  }

  public static MonthlyEventTypeSubTotal empty() {
    return new MonthlyEventTypeSubTotal(BillableEventType.MISC, "", Money.zero(CurrencyUnit.EUR));
  }
}
