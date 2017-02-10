package com.tngtech.qb;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.Objects;

public class MonthlyCustomerSubTotal extends MonthlySubTotal<String> {

  public MonthlyCustomerSubTotal(final String customer, final String month, final Money total) {
    super(customer, month, total);
  }

  public static MonthlyCustomerSubTotal empty() {
    return new MonthlyCustomerSubTotal("", "", Money.zero(CurrencyUnit.EUR));
  }
}
