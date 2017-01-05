package com.tngtech.qb;

import com.jgrier.flinkstuff.data.KeyedDataPoint;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.runtime.query.QueryableStateClient;
import org.apache.flink.runtime.query.netty.message.KvStateRequestSerializer;
import org.apache.flink.runtime.state.VoidNamespace;
import org.apache.flink.runtime.state.VoidNamespaceSerializer;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FlinkStateQueryService {
  private final QueryableStateClient client;

  private final JobID jobId;

  public FlinkStateQueryService() throws Exception {
    this("4f2df5529aed29c68224f30c157e270f", "src/test/resources");
  }

  public FlinkStateQueryService(String jobIdHex, String configDir) throws Exception {
    jobId = JobID.fromHexString(jobIdHex);
    client = new QueryableStateClient(GlobalConfiguration.loadConfiguration(configDir));
  }

  List<KeyedDataPoint<Long>> query(String key) throws Exception {
    final Future<byte[]> stateFuture =
        client.getKvState(jobId, "time-series", key.hashCode(), serialize(key));
    final byte[] serializedResult =
        Await.result(stateFuture, new FiniteDuration(10, TimeUnit.SECONDS));
    return deserialize(serializedResult);
  }

  private byte[] serialize(String key) throws IOException {
    TypeSerializer<String> keySerializer =
        TypeInformation.of(new TypeHint<String>() {}).createSerializer(null);
    return KvStateRequestSerializer.serializeKeyAndNamespace(
        key, keySerializer, VoidNamespace.INSTANCE, VoidNamespaceSerializer.INSTANCE);
  }

  private List<KeyedDataPoint<Long>> deserialize(byte[] serializedResult) throws IOException {
    return KvStateRequestSerializer.deserializeList(
        serializedResult,
        TypeInformation.of(new TypeHint<KeyedDataPoint<Long>>() {})
            .createSerializer(new ExecutionConfig()));
  }
}
