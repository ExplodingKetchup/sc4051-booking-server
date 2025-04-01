package org.tomato.bookie.distributedSystem.semantics;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.SerializationException;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

@Slf4j
public class AtLeastOnce implements InvocationSemantic {
    private static final int MAX_RETRIES = 3;
    private static final double FAILURE_RATE = 0.3; // 30% failure chance

    @Override
    public Response execute(Request request) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Response response = attemptProcess(request);
            
            if (response.isStatus()) {
                if (!request.isIdempotent() && attempt > 0) {
                    System.out.println("[WARNING] Potential duplicate execution of non-idempotent operation");
                }
                return response;
            }
            
            try {
                Thread.sleep(100 * (attempt + 1)); // Exponential backoff
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                try {
                    return Response.error(request.getRequestId(), "Operation interrupted");
                } catch (SerializationException ex) {
                    log.error("[execute] Error message serialization failed, message: [Operation interrupted]", ex);
                    return Response.error(request.getRequestId());
                }
            }
        }
        try {
            return Response.error(request.getRequestId(), "Max retries exceeded");
        } catch (SerializationException e) {
            log.error("[execute] Error message serialization failed, message: [Max retries exceeded]", e);
            return Response.error(request.getRequestId());
        }
    }

    private Response attemptProcess(Request request) {
        // Simulate random failures
        if (Math.random() < FAILURE_RATE) {
            try {
                return Response.error(request.getRequestId(), "Simulated processing failure");
            } catch (SerializationException e) {
                log.error("[attemptProcess] Error message serialization failed, message: [Simulated processing failure]", e);
                return Response.error(request.getRequestId());
            }
        }
        return Response.success(request.getRequestId(), 
            Map.of("operation", request.getOperation().name().getBytes(StandardCharsets.UTF_8),
                  "attempts", "succeeded after retries".getBytes(StandardCharsets.UTF_8)));
    }
}
