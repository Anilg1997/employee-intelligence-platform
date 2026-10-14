package com.employeeintelligence.infrastructure;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies the generated protobuf API and deadline-capable internal transport only. */
class InternalGrpcProofTest {
    @Test
    void internalWorkerReadinessHasTypedContractAndDeadline() throws Exception {
        var health = new HealthStatusManager();
        health.setStatus("report-worker", ServingStatus.SERVING);
        String name = InProcessServerBuilder.generateName();
        var server = InProcessServerBuilder.forName(name).directExecutor().addService(health.getHealthService()).build().start();
        var channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        try {
            var request = HealthCheckRequest.newBuilder().setService("report-worker").build();
            assertEquals(ServingStatus.SERVING, HealthGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(2, TimeUnit.SECONDS).check(request).getStatus());
            health.setStatus("report-worker", ServingStatus.NOT_SERVING);
            assertEquals(ServingStatus.NOT_SERVING, HealthGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(2, TimeUnit.SECONDS).check(request).getStatus());
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
