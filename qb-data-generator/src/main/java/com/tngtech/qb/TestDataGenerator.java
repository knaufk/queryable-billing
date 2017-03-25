package com.tngtech.qb;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.DAYS;

public class TestDataGenerator {
  private static final List<String> CUSTOMERS =
      ImmutableList.of(
          "Emma",
          "Olivia",
          "Sophia",
          "Ava",
          "Isabella",
          "Noah",
          "Liam",
          "Mason",
          "Jacob",
          "William");

  private static final int ERROR_MARGIN_MILLIS = 10000; // TODO necessary?

  private static final Integer DELAY_PER_MONTH = 30_000;

  private static final int EVENTS_PER_CUSTOMER_AND_MONTH = 100;
  private static final int SUM_PER_CUSTOMER_AND_MONTH_IN_CENTS = 10000;

  private static Random random = new Random();
  private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");

  public static void main(String[] args) throws InterruptedException {
    final String bootstrapServers = args[0];
    final Producer<String, String> producer = createKafkaProducer(bootstrapServers);

    List<BillableEvent.BillableEventType> types =
        Lists.newArrayList(BillableEvent.BillableEventType.values());

    int month = 0;
    while (true) {
      Thread.sleep(DELAY_PER_MONTH);
      final long beginningOfMonth = month * DAYS.toMillis(30);
      final long endOfMonth = beginningOfMonth + DAYS.toMillis(30);

      List<BillableEvent> monthlyEvents = Lists.newArrayList();

      for (final String customer : CUSTOMERS) {
        final List<Double> amounts =
            createRandomPartition(
                    EVENTS_PER_CUSTOMER_AND_MONTH, SUM_PER_CUSTOMER_AND_MONTH_IN_CENTS)
                .stream()
                .map(amountInCents -> ((double) amountInCents) / 100)
                .collect(Collectors.toList());

        for (Double amount : amounts) {
          final long timestamp =
              ThreadLocalRandom.current()
                  .nextLong(
                      beginningOfMonth + ERROR_MARGIN_MILLIS, endOfMonth - ERROR_MARGIN_MILLIS);
          final BillableEvent.BillableEventType nextType = types.get(random.nextInt(types.size()));

          monthlyEvents.add(
              new BillableEvent(timestamp, customer, Money.of(CurrencyUnit.EUR, amount), nextType));
        }
        monthlyEvents.sort(
            (o1, o2) -> (int) Math.signum(o1.getTimestampMs() - o2.getTimestampMs()));
      }

      for (final BillableEvent monthlyEvent : monthlyEvents) {
        final String nextRecord = formatForOutput(monthlyEvent);

        producer.send(
            new ProducerRecord<>(Constants.SRC_KAFKA_TOPIC, "" + random.nextInt(), nextRecord));

        //Write 10% of Records to stdout for demo
        if (random.nextFloat() < 0.1) {
          final String nextTimePretty =
              new DateTime(monthlyEvent.getTimestampMs()).toString(timeFormatter);
          System.out.println(formatForOutput(monthlyEvent));
        }
      }

      month++;
    }
  }

  private static String formatForOutput(BillableEvent event) {
    return Joiner.on(",")
        .join(
            event.getTimestampMs(),
            event.getCustomer(),
            event.getAmount().getAmountMajor() + "." + event.getAmount().getAmountMinorInt(),
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
        final int amount = random.nextInt((finalSum - sum) / 2) + 1;
        partition.add(amount);
        sum += amount;
      }
    }
    partition.add(finalSum - sum);
    return partition;
  }
}
