package com.tngtech.qb;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;

public class SimpleBillableEventSource extends RichParallelSourceFunction<BillableEvent> {
  private volatile boolean running = true;

  // Checkpointed State
  private volatile long currentTimeMs = 0;
  private BillableEventGenerator generator;

  @Override
  public void open(Configuration parameters) throws Exception {
    generator = new BillableEventGenerator(99, 100, 1000);
    super.open(parameters);
  }

  @Override
  public void run(SourceContext<BillableEvent> ctx) throws Exception {
    while (running) {
      synchronized (ctx.getCheckpointLock()) {
        BillableEvent next = generator.next();
        ctx.collectWithTimestamp(next, next.getTimestampMs());
        ctx.emitWatermark(new Watermark(next.getTimestampMs() - generator.getRegularDelayMs()));
      }
      Thread.sleep(50);
    }
  }

  @Override
  public void cancel() {
    running = false;
  }
}
