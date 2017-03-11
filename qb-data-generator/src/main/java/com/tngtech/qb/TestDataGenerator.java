package com.tngtech.qb;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class TestDataGenerator {

  private static final double MIN_AMOUNT = 0.0;
  private static final double MAX_AMOUNT = 99.99;

  public static final Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);

  static final List<String> CUSTOMERS =
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

  private static Producer<String, String> producer;
  private static DecimalFormat df = new DecimalFormat("#.##");
  private static Random random = new Random();
  private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");

  public static void main(String[] args) throws InterruptedException {
    final Integer delay;
    final String bootstrapServers = args[0];
    if (args.length != 2) {
      System.out.println(
          "Using default delay of 10ms, to use another value, give it as first argument.");
      delay = 10;
    } else {
      delay = Integer.valueOf(args[1]);
    }

    producer = createKafkaProducer(bootstrapServers);

    List<BillableEvent.BillableEventType> types =
        Lists.newArrayList(BillableEvent.BillableEventType.values());

    while (true) {
      Thread.sleep(delay);
      final String nextType = types.get(random.nextInt(types.size())).toString();
      final String nextCustomer = CUSTOMERS.get(random.nextInt(CUSTOMERS.size()));
      final String nextAmount = getNextAmount();
      final long nextTime = System.currentTimeMillis();
      final String nextTimePretty = new DateTime(nextTime).toString(timeFormatter);

      final String nextRecord = Joiner.on(",").join(nextTime, nextCustomer, nextAmount, nextType);

      producer.send(
          new ProducerRecord<>(Constants.SRC_KAFKA_TOPIC, "" + random.nextInt(), nextRecord));

      //Write 10% of Records to stdout for demo
      if (random.nextFloat() < 0.1) {
        System.out.println(Joiner.on(",").join(nextTimePretty, nextCustomer, nextAmount, nextType));
      }
    }
  }

  private static String getNextAmount() {
    return df.format(MIN_AMOUNT + (random.nextDouble() * (MAX_AMOUNT - MIN_AMOUNT)));
  }

  private static Producer<String, String> createKafkaProducer(final String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return new KafkaProducer<>(props);
  }
}
