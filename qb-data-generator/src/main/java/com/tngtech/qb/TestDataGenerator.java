package com.tngtech.qb;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;
import java.util.Random;

public class TestDataGenerator {
  private static final double MIN_AMOUNT = 0.0;
  private static final double MAX_AMOUNT = 99.99;

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

    Random random = new Random();

    Producer<String, String> producer = createKafkaProducer(bootstrapServers);

    List<String> customers =
        Lists.newArrayList(
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
    List<BillableEvent.BillableEventType> types =
        Lists.newArrayList(BillableEvent.BillableEventType.values());

    for (int i = 0; i < 1_000_000_000; i++) {
      Thread.sleep(delay);
      final String type = types.get(random.nextInt(types.size())).toString();
      final String customer = customers.get(random.nextInt(customers.size()));
      final double amount = MIN_AMOUNT + (random.nextDouble() * (MAX_AMOUNT - MIN_AMOUNT));

      producer.send(
          new ProducerRecord<>(
              Constants.SRC_KAFKA_TOPIC,
              "" + random.nextInt(),
              Joiner.on(",").join(System.currentTimeMillis(), customer, amount, type)));
    }
    producer.close();
  }

  private static Producer<String, String> createKafkaProducer(final String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return new KafkaProducer<>(props);
  }
}
