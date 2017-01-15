package com.tngtech.qb

import com.google.common.collect.ImmutableMap
import groovy.io.FileType
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.windowing.time.Time
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.util.stream.LongStream

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.collection.IsCollectionWithSize.hasSize
import static org.hamcrest.core.AnyOf.anyOf
import static org.hamcrest.core.IsCollectionContaining.hasItem

class QueryableBillingJobSpec extends Specification {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    private QueryableBillingJob job
    private StreamExecutionEnvironment env

    def setup() {
        env = StreamExecutionEnvironment.getExecutionEnvironment()
        job = Spy(QueryableBillingJob, constructorArgs: [this.env, ParameterTool.fromMap(ImmutableMap.of("output", temporaryFolder.getRoot().getPath()))]) {
            billableEvents() >> createTestSource()
        }
    }

    def "final invoices are written"() {
        setup:
        job.run()
        when:
        def outputLines = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES) { file ->
            if (!Files.isHidden(file.toPath())) {
                outputLines.addAll(file.readLines())
            }
        }

        print(outputLines)

        then:
        assertThat(outputLines, anyOf(hasSize(3), hasSize(6)))
        ["Anton", "Berta", "Charlie"].forEach({ assertThat(outputLines, hasItem(containsString(it))) })

    }

    static class TestTimestampAssigner extends BoundedOutOfOrdernessTimestampExtractor<BillableEvent> {
        TestTimestampAssigner(final Time maxOutOfOrderness) {
            super(maxOutOfOrderness)
        }

        @Override
        long extractTimestamp(final BillableEvent element) {
            return element.getTimestampMs()
        }
    }

    private SingleOutputStreamOperator<BillableEvent> createTestSource() {
        env.fromCollection(
                LongStream.range(1, 1000)
                        .collect({ new BillableEvent().setEuroAmount(it) }))
                .assignTimestampsAndWatermarks(
                new TestTimestampAssigner(Time.seconds(1)))
    }

}
