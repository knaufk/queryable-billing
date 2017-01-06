package com.jgrier.flinkstuff.sources;

import com.tngtech.qb.BillableEvent;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.checkpoint.CheckpointedAsynchronously;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class TimestampSource extends RichParallelSourceFunction<BillableEvent>
    implements CheckpointedAsynchronously<Long> {
  private final int periodMs;
  private final int slowdownFactor;
  private volatile boolean running = true;

  // Checkpointed State
  private volatile long currentTimeMs = 0;

  public TimestampSource(int periodMs, int slowdownFactor) {
    this.periodMs = periodMs;
    this.slowdownFactor = slowdownFactor;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    long now = System.currentTimeMillis();
    if (currentTimeMs == 0) {
      currentTimeMs = now - (now % 1000); // floor to second boundary
    }
  }

  @Override
  public void run(SourceContext<BillableEvent> ctx) throws Exception {
    while (running) {
      synchronized (ctx.getCheckpointLock()) {
        ctx.collectWithTimestamp(
            new BillableEvent(currentTimeMs, "", Money.of(CurrencyUnit.EUR, 0)), currentTimeMs);
        ctx.emitWatermark(new Watermark(currentTimeMs));
        currentTimeMs += periodMs;
      }
      timeSync();
    }
  }

  @Override
  public void cancel() {
    running = false;
  }

  @Override
  public Long snapshotState(long checkpointId, long checkpointTimestamp) throws Exception {
    return currentTimeMs;
  }

  @Override
  public void restoreState(Long state) throws Exception {
    currentTimeMs = state;
  }

  private void timeSync() throws InterruptedException {
    // Sync up with real time
    long realTimeDeltaMs = currentTimeMs - System.currentTimeMillis();
    long sleepTime = periodMs + realTimeDeltaMs + randomJitter();

    if (slowdownFactor != 1) {
      sleepTime = periodMs * slowdownFactor;
    }

    if (sleepTime > 0) {
      Thread.sleep(sleepTime);
    }
  }

  private long randomJitter() {
    double sign = -1.0;
    if (Math.random() > 0.5) {
      sign = 1.0;
    }
    return (long) (Math.random() * periodMs * sign);
  }
}
