package com.tngtech.qb.dto;

import com.tngtech.qb.MonthlyCustomerSubTotal;

public class MonthlyCustomerSubTotalResponse {
    private final String customer;
    private final String month;
    private final String totalEur;


    public MonthlyCustomerSubTotalResponse(final String customer, final String month, final String totalEur) {
        this.customer = customer;
        this.month = month;
        this.totalEur = totalEur;
    }

    public MonthlyCustomerSubTotalResponse(MonthlyCustomerSubTotal subTotal) {
        customer = subTotal.getBasis();
        month = subTotal.getMonth();
        totalEur = subTotal.getTotal().getAmount().toString();
    }

    public String getCustomer() {
        return customer;
    }

    public String getMonth() {
        return month;
    }

    public String getTotalEur() {
        return totalEur;
    }
}
