package com.tngtech.qb;

import org.springframework.stereotype.Service;

@Service
public interface StateQueryService {

  MonthlyCustomerSubTotal findOne(String customer) throws Exception;

  MonthlyEventTypeSubTotal findOne(BillableEvent.BillableEventType type) throws Exception;
}
