package com.tngtech.qb;

import com.google.common.collect.Maps;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
@Profile("standalone")
public class StandaloneStateQueryService implements StateQueryService {

  private static final Random rnd = new Random();

  private Map<String, Money> subTotals = Maps.newConcurrentMap();

  @Override
  public MonthlySubtotalByCategory queryCustomer(final String customer) throws Exception {
    Money total = subTotals.getOrDefault(customer, Money.of(CurrencyUnit.EUR, 0.0));
    subTotals.put(customer, total.plusMinor(rnd.nextInt(500)));
    return new MonthlySubtotalByCategory(customer, "someMonth", total);
  }

  @Override
  public MonthlySubtotalByCategory queryType(final String type) throws Exception {
    return new MonthlySubtotalByCategory(type, "someMonth", Money.of(CurrencyUnit.EUR, 10.12));
  }
}
