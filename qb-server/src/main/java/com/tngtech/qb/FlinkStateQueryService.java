package com.tngtech.qb;

import com.tngtech.qb.BillableEvent.BillableEventType;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class FlinkStateQueryService {
  private final QueryableStateClient client;
  private final JobID jobId;

  public FlinkStateQueryService(
      @Value("${flink.jobIdHex}") String jobIdHex,
      @Value("${flink.configDir}") String flinkConfigDir)
      throws Exception {
    jobId = JobID.fromHexString(jobIdHex);
    client = new QueryableStateClient(GlobalConfiguration.loadConfiguration(flinkConfigDir));
  }

  MonthlyCustomerSubTotal findOne(String customer) throws Exception {
    final Future<byte[]> stateFuture =
        client.getKvState(
            jobId, Constants.PER_CUSTOMER_STATE_NAME, customer.hashCode(), serializeCustomer(customer));
    final byte[] serializedResult =
        Await.result(stateFuture, new FiniteDuration(10, TimeUnit.SECONDS));
    return deserializeCustomer(serializedResult);
  }

  public MonthlyEventTypeSubTotal findOne(final BillableEventType type) throws Exception {
    final Future<byte[]> stateFuture =
            client.getKvState(
                    jobId, Constants.PER_EVENT_TYPE_STATE_NAME, type.hashCode(), serializeEventType(type));
    final byte[] serializedResult =
            Await.result(stateFuture, new FiniteDuration(10, TimeUnit.SECONDS));
    return deserializeEventType(serializedResult);
  }

  private byte[] serializeCustomer(String key) throws IOException {
    TypeSerializer<String> keySerializer =
        TypeInformation.of(new TypeHint<String>() {}).createSerializer(null);
    return KvStateRequestSerializer.serializeKeyAndNamespace(
        key, keySerializer, VoidNamespace.INSTANCE, VoidNamespaceSerializer.INSTANCE);
  }

  private byte[] serializeEventType(final BillableEventType type) throws IOException {
    TypeSerializer<BillableEventType> keySerializer =
            TypeInformation.of(new TypeHint<BillableEventType>() {}).createSerializer(null);
    return KvStateRequestSerializer.serializeKeyAndNamespace(
            type, keySerializer, VoidNamespace.INSTANCE, VoidNamespaceSerializer.INSTANCE);
  }

  private MonthlyCustomerSubTotal deserializeCustomer(byte[] serializedResult) throws IOException {
    return KvStateRequestSerializer.deserializeValue(
        serializedResult,
        TypeInformation.of(new TypeHint<MonthlyCustomerSubTotal>() {})
            .createSerializer(new ExecutionConfig()));
  }

  private MonthlyEventTypeSubTotal deserializeEventType(byte[] serializedResult) throws IOException {
    return KvStateRequestSerializer.deserializeValue(
            serializedResult,
            TypeInformation.of(new TypeHint<MonthlyEventTypeSubTotal>() {})
                           .createSerializer(new ExecutionConfig()));
  }
}
