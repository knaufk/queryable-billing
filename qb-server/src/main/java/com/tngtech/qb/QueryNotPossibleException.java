package com.tngtech.qb;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Error querying Flink state.")
class QueryNotPossibleException extends RuntimeException {
  QueryNotPossibleException(Object key) {
    super("Could not query for key " + key);
  }
}
