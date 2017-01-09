package com.tngtech.qb;

import com.google.common.annotations.VisibleForTesting;
import com.jgrier.flinkstuff.sources.TimestampSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class QueryableBillingJob {
  @VisibleForTesting
  final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

  private QueryableBillingJob() {
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
  }

  public static void main(String[] args) throws Exception {
    new QueryableBillingJob().run();
  }

  void run() throws Exception {
    invoice(readBillableEvents());
    env.execute("Queryable Billing Job");
  }

  private SingleOutputStreamOperator<BillableEvent> readBillableEvents() {
    return getSource()
        .flatMap(
            new FlatMapFunction<BillableEvent, BillableEvent>() {

              @Override
              public void flatMap(BillableEvent value, Collector<BillableEvent> out)
                  throws Exception {
                Lists.newArrayList("Anton", "Berta", "Charlie")
                    .forEach(
                        c -> out.collect(value.withNewAmount(nextRandomAmount()).withCustomer(c)));
              }
            });
  }

  @VisibleForTesting
  DataStream<BillableEvent> getSource() {
    return env.addSource(new TimestampSource(100, 1));
  }

  private static double nextRandomAmount() {
    return Math.random() * 100;
  }

  private void invoice(SingleOutputStreamOperator<BillableEvent> billableEvents) {
    final WindowedStream<BillableEvent, Tuple, TimeWindow> windowed =
        billableEvents.keyBy("customer").window(TumblingEventTimeWindows.of(Time.seconds(10)));

    final SingleOutputStreamOperator<BillableEvent> summedPreviews =
        windowed
            .trigger(CountTrigger.of(1))
            .fold(
                new Tuple2<>("", Money.zero(CurrencyUnit.EUR)),
                (FoldFunction<BillableEvent, Tuple2<String, Money>>)
                    (accumulator, value) ->
                        new Tuple2<>(value.getCustomer(), value.getAmount().plus(accumulator.f1)),
                (WindowFunction<Tuple2<String, Money>, BillableEvent, Tuple, TimeWindow>)
                    (tuple, window, input, out) -> {
                      final Tuple2<String, Money> customerWithAmount = input.iterator().next();
                      out.collect(
                          new BillableEvent(
                              window.getStart(), customerWithAmount.f0, customerWithAmount.f1));
                    },
                TypeInformation.of(new TypeHint<Tuple2<String, Money>>() {}),
                TypeInformation.of(new TypeHint<BillableEvent>() {}));

    summedPreviews.keyBy("customer").flatMap(new EndpointForQueries());

    billableEvents
        .keyBy((KeySelector<BillableEvent, Integer>) value -> Constants.CUSTOMERS_KEY)
        .flatMap(new EndpointForCustomerQueries());
  }
}
