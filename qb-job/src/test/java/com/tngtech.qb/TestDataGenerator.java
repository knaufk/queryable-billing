package com.tngtech.qb;

import com.google.common.collect.Lists;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;
import java.util.Random;

public class TestDataGenerator {
  public static void main(String[] args) throws InterruptedException {
    Random random = new Random();

    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    Producer<String, String> producer = new KafkaProducer<>(props);

    List<String> customers = Lists.newArrayList("Anton", "Berta", "Charlie");

    for (int i = 0; i < 1000; i++) {
      Thread.sleep(100);
      //TODO integrate event types
      //def types = BillableEvent.BillableEventType.values().toList()
      String customer = customers.get(random.nextInt(customers.size()));

      producer.send(
          new ProducerRecord<>(
              Constants.SRC_KAFKA_TOPIC,
              "" + i,
              System.currentTimeMillis() + "," + customer + "," + i));
    }
    producer.close();
  }
}
