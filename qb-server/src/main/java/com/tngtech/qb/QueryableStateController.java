package com.tngtech.qb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryableStateController {
  private FlinkStateQueryService queryService;

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryableStateController.class);

  @Autowired
  public QueryableStateController(FlinkStateQueryService queryService) {
    this.queryService = queryService;
  }

  @RequestMapping(path = "/customers/{customer}", method = RequestMethod.GET)
  public MonthlyCustomerSubTotal queryByCustomer(@PathVariable String customer) {
    try {
      return queryService.findOne(customer);
    } catch (Exception e) {
      LOGGER.error("Error querying customer", e);
      throw new QueryNotPossibleException(customer);
    }
  }

  @RequestMapping(path = "/types/{type}", method = RequestMethod.GET)
  public MonthlyEventTypeSubTotal queryByType(@PathVariable String type) {
    try {
      return queryService.findOne(BillableEvent.BillableEventType.valueOf(type.toUpperCase()));
    } catch (Exception e) {
      LOGGER.error("Error querying type", e);
      throw new QueryNotPossibleException(type);
    }
  }
}
