package com.tngtech.qb;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("standalone")
public class StandaloneStateQueryService implements StateQueryService {

    @Override
    public MonthlyCustomerSubTotal findOne(final String customer) throws Exception {
        return new MonthlyCustomerSubTotal(customer, "someMonth", Money.of(CurrencyUnit.EUR, 10.10));
    }

    @Override
    public MonthlyEventTypeSubTotal findOne(final BillableEvent.BillableEventType type) throws Exception {
        return new MonthlyEventTypeSubTotal(type, "someMonth", Money.of(CurrencyUnit.EUR, 10.12));
    }
}
