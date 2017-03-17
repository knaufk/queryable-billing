package com.tngtech.qb;

import org.apache.flink.streaming.connectors.fs.Clock;
import org.apache.flink.streaming.connectors.fs.bucketing.Bucketer;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.ObjectInputStream;

public class MonthBucketer implements Bucketer<MonthlyCustomerSubTotal> {

  private static final long serialVersionUID = 1L;

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
  }

  @Override
  public Path getBucketPath(
      final Clock clock, final Path basePath, final MonthlyCustomerSubTotal element) {
    return new Path(basePath + "/" + element.getMonth());
  }
}
