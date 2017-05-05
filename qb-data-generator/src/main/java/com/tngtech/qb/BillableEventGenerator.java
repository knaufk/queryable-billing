package com.tngtech.qb;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tngtech.qb.BillableEvent.BillableEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.money.Money;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.joda.money.CurrencyUnit.*;
import static org.joda.money.format.MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING;

@Slf4j
public class BillableEventGenerator {

  private static final Map<String, Integer> CUSTOMERS =
      ImmutableMap.<String, Integer>builder()
          .put("Emma", 10000)
          .put("Olivia", 8000)
          .put("Sophia", 12000)
          .put("Ava", 6000)
          .put("Isa", 2000)
          .put("Noah", 20000)
          .put("William", 25000)
          .put("Liam", 3000)
          .put("Jacob", 4000)
          .build();

  private static final int DELAY_PER_MONTH = 30_000;
  private static final int EVENTS_PER_CUSTOMER_AND_MONTH = 10_000;

  private static final long MAX_OUT_OF_ORDERNESS = HOURS.toMillis(12);
  private static final float OUT_OF_ORDERNESS_COEFFICIENT = 0.1f;

  private static final long MAX_LATENESS = DAYS.toMillis(3);

  private static Random random = new Random();
  private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");
  private static MoneyFormatter moneyFormatter =
      new MoneyFormatterBuilder().appendAmount(ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter();

  private static final List<BillableEventType> BILLABLE_EVENT_TYPES =
      Lists.newArrayList(BillableEventType.values());

  public static void main(String[] args) throws InterruptedException {

    final String bootstrapServers = args[0];

    final Producer<String, String> producer = createKafkaProducer(bootstrapServers);

    int year = 0;
    while (true) {

      final long startOfYear = year * DAYS.toMillis(360);

      List<BillableEvent> eventsThisYear = Lists.newArrayList();

      for (int month = 0; month < 11; month++) {

        final long startOfMonth = startOfYear + month * DAYS.toMillis(30);
        final long endOfMonth = startOfMonth + DAYS.toMillis(30);

        eventsThisYear.addAll(getEventsForMonth(startOfMonth, endOfMonth));
      }

      eventsThisYear.sort((o1, o2) -> (int) Math.signum(o1.getTimestampMs() - o2.getTimestampMs()));
      addEventTimeSkewAndLateness(eventsThisYear);

      for (final BillableEvent event : eventsThisYear) {

        producer.send(
            new ProducerRecord<>(Constants.SRC_KAFKA_TOPIC, event.getCustomer(), format(event)));

        maybeLog(event);

        Thread.sleep(DELAY_PER_MONTH * 12 / eventsThisYear.size());
      }

      year++;
    }
  }

  private static void addEventTimeSkewAndLateness(final List<BillableEvent> events) {

    for (int i = 0; i < events.size() - 1; i = i + 2) {
      long lowTimestamp = events.get(i).getTimestampMs();
      long highTimestamp = events.get(i + 1).getTimestampMs();

      if (lowTimestamp + MAX_OUT_OF_ORDERNESS > highTimestamp
          && random.nextFloat() < OUT_OF_ORDERNESS_COEFFICIENT) {
        Collections.swap(events, i, i + 1);
      }
    }

    for (int i = 0; i < events.size() - 50; i = i + 51) {
      long lowTimestamp = events.get(i).getTimestampMs();
      long highTimestamp = events.get(i + 50).getTimestampMs();

      if (lowTimestamp + MAX_LATENESS > highTimestamp) {
        Collections.swap(events, i, i + 50);
      }
    }
  }

  private static void maybeLog(final BillableEvent event) {
    if (random.nextFloat() < 1) {
      log.debug(
          "{}, {}, {}, {}",
          new DateTime(event.getTimestampMs()).toString(timeFormatter),
          event.getCustomer(),
          moneyFormatter.print(event.getAmount()),
          event.getType());
    }
  }

  private static List<BillableEvent> getEventsForMonth(
      final long beginningOfMonth, final long endOfMonth) {
    List<BillableEvent> monthlyEvents = Lists.newArrayList();

    for (final Map.Entry<String, Integer> customerAndTotalAmount : CUSTOMERS.entrySet()) {

      String customer = customerAndTotalAmount.getKey();
      Integer totalAmount = customerAndTotalAmount.getValue();

      final List<Integer> amounts =
          createRandomPartition(EVENTS_PER_CUSTOMER_AND_MONTH, totalAmount);

      for (Integer amount : amounts) {
        monthlyEvents.add(
            new BillableEvent(
                randomTime(beginningOfMonth, endOfMonth),
                customer,
                Money.ofMinor(EUR, amount),
                randomType()));
      }
    }

    return monthlyEvents;
  }

  private static long randomTime(final long beginningOfMonth, final long endOfMonth) {
    return ThreadLocalRandom.current().nextLong(beginningOfMonth, endOfMonth);
  }

  private static BillableEventType randomType() {
    final int noOfTypes = BILLABLE_EVENT_TYPES.size();
    return BILLABLE_EVENT_TYPES.get(random.nextInt(noOfTypes));
  }

  private static String format(BillableEvent event) {
    return Joiner.on(",")
        .join(
            event.getTimestampMs(),
            event.getCustomer(),
            moneyFormatter.print(event.getAmount()),
            event.getType());
  }

  private static Producer<String, String> createKafkaProducer(final String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return new KafkaProducer<>(props);
  }

  private static List<Integer> createRandomPartition(int count, int finalSum) {
    final LinkedList<Integer> partition = new LinkedList<>();
    int sum = 0;
    for (int i = 0; i < count - 1; i++) {
      if (sum < finalSum - 2) {
        final int amount = random.nextInt((finalSum - sum) / 10 + 1) + 1;
        partition.add(amount);
        sum += amount;
      }
    }
    partition.add(finalSum - sum);
    return partition;
  }
}
