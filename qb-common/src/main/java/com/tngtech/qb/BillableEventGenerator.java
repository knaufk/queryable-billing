package com.tngtech.qb;

import com.tngtech.qb.BillableEvent.BillableEventType;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.List;
import java.util.Random;

import static com.tngtech.qb.BillableEvent.BillableEventType.*;

public class BillableEventGenerator {

  private static final int MAX_AMOUNT_CENT = 1_000;
  private static final int MIN_AMOUNT_CENT = 49;
  private static final int NO_OF_CUSTOMERS = 100;
  private static final double RATIO_LATE_EVENTS = 0.005;

  private final int regularDelayMs;
  private final int maxDelayMs;
  private final Random rnd;
  private final List<Name> customers;

  /**
   * A generator for BillableEvents. Can be used in a Flink Source directly, or in a Kafka producer.
   *
   * @param seed source of randomness
   * @param regularDelayMs each event will have a delay between 0 and regularDelayMs-1
   * @param maxDelayMs a small fraction of events will have an additional delay of maxDelayMs
   */
  public BillableEventGenerator(long seed, int regularDelayMs, int maxDelayMs) {
    rnd = new Random(seed);
    this.regularDelayMs = regularDelayMs;
    this.maxDelayMs = maxDelayMs;
    NameGenerator generator = new NameGenerator();
    customers = generator.generateNames(NO_OF_CUSTOMERS);
  }

  public BillableEvent next() {
    return new BillableEvent(nextTimestamp(), nextCustomer(), nextAmount(), nextEventType());
  }

  private BillableEventType nextEventType() {
    return values()[rnd.nextInt(count())];
  }

  private Money nextAmount() {
    return Money.ofMinor(
        CurrencyUnit.EUR, rnd.nextInt(MAX_AMOUNT_CENT - MIN_AMOUNT_CENT) + MIN_AMOUNT_CENT);
  }

  private String nextCustomer() {
    Name customer = customers.get(rnd.nextInt(NO_OF_CUSTOMERS));

    return customer.getFirstName();
  }

  private long nextTimestamp() {
    long now = System.currentTimeMillis();

    int regularDelay = rnd.nextInt(regularDelayMs);
    int potentialExtraDelay =
        rnd.nextDouble() > RATIO_LATE_EVENTS ? 0 : maxDelayMs - regularDelayMs;

    return now - regularDelay - potentialExtraDelay;
  }

  public int getRegularDelayMs() {
    return regularDelayMs;
  }

  public int getMaxDelayMs() {
    return maxDelayMs;
  }
}
