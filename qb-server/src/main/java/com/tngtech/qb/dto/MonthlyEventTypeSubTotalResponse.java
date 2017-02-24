package com.tngtech.qb.dto;

import com.tngtech.qb.BillableEvent.BillableEventType;
import com.tngtech.qb.MonthlyEventTypeSubTotal;

public class MonthlyEventTypeSubTotalResponse {
    private final BillableEventType type;
    private final String            month;
    private final String            totalEur;


    public MonthlyEventTypeSubTotalResponse(final BillableEventType customer, final String month, final String totalEur) {
        this.type = customer;
        this.month = month;
        this.totalEur = totalEur;
    }

    public MonthlyEventTypeSubTotalResponse(MonthlyEventTypeSubTotal subTotal) {
        type = subTotal.getBasis();
        month = subTotal.getMonth();
        totalEur = subTotal.getTotal().getAmount().toString();
    }

    public BillableEventType getType() {
        return type;
    }

    public String getMonth() {
        return month;
    }

    public String getTotalEur() {
        return totalEur;
    }
}
