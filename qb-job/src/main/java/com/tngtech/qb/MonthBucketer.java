package com.tngtech.qb;

import org.apache.flink.streaming.connectors.fs.Clock;
import org.apache.flink.streaming.connectors.fs.bucketing.Bucketer;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.ObjectInputStream;

public class MonthBucketer implements Bucketer<MonthlySubtotalByCategory> {

  private static final long serialVersionUID = 1L;

  @Override
  public Path getBucketPath(
      final Clock clock, final Path basePath, final MonthlySubtotalByCategory element) {
    return new Path(basePath + "/" + element.getFormattedMonth());
  }
}
