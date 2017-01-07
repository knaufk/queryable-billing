package com.tngtech.qb;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.util.StreamingMultipleProgramsTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RunWith(MockitoJUnitRunner.class)
public class QueryableBillingJobTest extends StreamingMultipleProgramsTestBase {
  @Spy private QueryableBillingJob job;

  @Test
  public void jobRuns() throws Exception {
    Mockito.when(job.getSource()).thenReturn(mockSource());
    job.run();
  }

  private SingleOutputStreamOperator<BillableEvent> mockSource() {
    return job.env
        .fromCollection(
            LongStream.range(1, 1000)
                .mapToObj(i -> new BillableEvent().withNewAmount(i))
                .collect(Collectors.toList()))
        .assignTimestampsAndWatermarks(
            new BoundedOutOfOrdernessTimestampExtractor<BillableEvent>(Time.seconds(1)) {
              @Override
              public long extractTimestamp(BillableEvent element) {
                return System.currentTimeMillis();
              }
            });
  }
}
