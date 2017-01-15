package com.tngtech.qb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class QueryableStateController {
  private FlinkStateQueryService queryService;

  @Autowired
  public QueryableStateController(FlinkStateQueryService queryService) {
    this.queryService = queryService;
  }

  @RequestMapping(path = "/subtotals/{customer}", method = RequestMethod.GET)
  public @ResponseBody MonthlyTotal query(@PathVariable String customer) {
    try {
      return queryService.findOne(customer);
    } catch (Exception e) {
      throw new QueryNotPossibleException(customer);
    }
  }
}
