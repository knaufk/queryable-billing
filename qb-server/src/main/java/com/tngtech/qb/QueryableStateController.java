package com.tngtech.qb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/query")
public class QueryableStateController {
  private FlinkStateQueryService queryService;

  @Autowired
  public QueryableStateController(FlinkStateQueryService queryService) {
    this.queryService = queryService;
  }

  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody String query(
      @RequestParam(value = "customer", required = false, defaultValue = "Anton") String customer) {
    try {
      return queryService.query(customer).toString();
    } catch (Exception e) {
      throw new QueryNotPossibleException(customer);
    }
  }
}
