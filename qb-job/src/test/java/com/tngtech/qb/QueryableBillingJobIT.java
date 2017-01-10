package com.tngtech.qb;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.util.StreamingMultipleProgramsTestBase;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

@RunWith(MockitoJUnitRunner.class)
public class QueryableBillingJobIT extends StreamingMultipleProgramsTestBase {
  @ClassRule public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Spy
  private QueryableBillingJob job =
      new QueryableBillingJob(
          ParameterTool.fromMap(ImmutableMap.of("output", temporaryFolder.getRoot().getPath())));

  @Before
  public void runJob() throws Exception {
    Mockito.when(job.getSource()).thenReturn(mockSource());
    job.run();
  }

  @Test
  public void finalInvoicesAreWritten() throws IOException {
    try (Stream<Path> paths = Files.walk(Paths.get(temporaryFolder.getRoot().getPath()))) {
      List<String> outputLines = new LinkedList<>();
      paths.forEach(
          filePath -> {
            try {
              if (Files.isRegularFile(filePath) && !Files.isHidden(filePath)) {
                outputLines.addAll(FileUtils.readLines(filePath.toFile()));
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
      assertThat(outputLines, anyOf(hasSize(3), hasSize(6)));
      Lists.newArrayList("Anton", "Berta", "Charlie")
          .forEach(s -> assertThat(outputLines, hasItem(containsString(s))));
    }
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
