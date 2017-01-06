package com.tngtech.qb;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Could not query")
class QueryNotPossibleException extends RuntimeException {
  QueryNotPossibleException(String customer) {
    super("Could not query for custome " + customer);
  }
}
