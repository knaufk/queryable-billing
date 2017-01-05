package com.tngtech.qb;

import org.apache.flink.streaming.util.StreamingMultipleProgramsTestBase;
import org.junit.Test;

public class QueryableBillingJobTest extends StreamingMultipleProgramsTestBase {
    @Test
    public void jobRuns() throws Exception {
        final QueryableBillingJob job = new QueryableBillingJob();
        job.run();
    }
}