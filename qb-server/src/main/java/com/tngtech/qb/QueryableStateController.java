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

import static com.tngtech.qb.BillableEvent.*;

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
      LOGGER.info("Querying for customer {}", customer);
      return new MonthlyCustomerSubTotalResponse(queryService.queryCustomer(customer));
    } catch (Exception e) {
      LOGGER.error("Error querying customer", e);
      throw new QueryNotPossibleException(customer);
    }
  }

  @RequestMapping(path = "/types/{type}", method = RequestMethod.GET)
  public MonthlyEventTypeSubTotalResponse queryByType(@PathVariable String type) {
    try {
      return new MonthlyEventTypeSubTotalResponse(queryService.queryType(type.toUpperCase()));
    } catch (Exception e) {
      LOGGER.error("Error querying type", e);
      throw new QueryNotPossibleException(type);
    }
  }
}
