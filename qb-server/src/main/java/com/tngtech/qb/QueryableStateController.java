package com.tngtech.qb;

import com.tngtech.qb.dto.MonthlyCustomerSubTotalResponse;
import com.tngtech.qb.dto.MonthlyEventTypeSubTotalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class QueryableStateController {

  private StateQueryService queryService;

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryableStateController.class);

  @Autowired
  public QueryableStateController(StateQueryService queryService) {
    this.queryService = queryService;
  }

  @RequestMapping(path = "/customers/{customer}", method = RequestMethod.GET)
  public MonthlyCustomerSubTotalResponse queryByCustomer(@PathVariable String customer) {
    try {
      return new MonthlyCustomerSubTotalResponse(queryService.findOne(customer));
    } catch (Exception e) {
      LOGGER.error("Error querying customer", e);
      throw new QueryNotPossibleException(customer);
    }
  }

  @RequestMapping(path = "/types/{type}", method = RequestMethod.GET)
  public MonthlyEventTypeSubTotalResponse queryByType(@PathVariable String type) {
    try {
      return new MonthlyEventTypeSubTotalResponse(queryService.findOne(BillableEvent.BillableEventType.valueOf(type.toUpperCase())));
    } catch (IllegalArgumentException e) {
      throw new QueryNotPossibleException("The supplied type \"" + type + "\" does not exist.");
    } catch (Exception e) {
      LOGGER.error("Error querying type", e);
      throw new QueryNotPossibleException(type);
    }
  }
}
