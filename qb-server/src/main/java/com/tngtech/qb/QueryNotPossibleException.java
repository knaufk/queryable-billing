package com.tngtech.qb;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Error Querying Current Total!")
class QueryNotPossibleException extends RuntimeException {
  QueryNotPossibleException(Object key) {
    super("Could not query for key " + key);
  }

  QueryNotPossibleException(String reason) {
    super(reason);
  }
}
