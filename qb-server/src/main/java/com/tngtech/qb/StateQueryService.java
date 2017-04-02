package com.tngtech.qb;

import org.springframework.stereotype.Service;

@Service
public interface StateQueryService {

  MonthlySubtotalByCategory queryCustomer(String customer) throws Exception;

  MonthlySubtotalByCategory queryType(String type) throws Exception;
}
