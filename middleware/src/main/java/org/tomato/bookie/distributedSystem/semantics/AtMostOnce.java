package org.tomato.bookie.distributedSystem.semantics;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.SerializationException;
import org.tomato.bookie.distributedSystem.faultolerance.Deduplicator;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

@Slf4j
public class AtMostOnce implements InvocationSemantic {
    private final Deduplicator deduplicator;

    public AtMostOnce(Deduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    @Override
    public Response execute(Request request) {
        if (deduplicator.isDuplicate(request.getRequestId())) {
            try {
                return Response.error(request.getRequestId(), "Duplicate request detected");
            } catch (SerializationException e) {
                log.error("[execute] Error message serialization failed, message: [Duplicate request detected]", e);
                return Response.error(request.getRequestId());
            }
        }

        deduplicator.recordRequest(request.getRequestId());
        return simulateProcessing(request);
    }
        
    private Response simulateProcessing(Request request) {
        // Simulated processing - would be real server call in actual implementation
        return Response.success(request.getRequestId(), 
            Map.of("operation", request.getOperation().name().getBytes(StandardCharsets.UTF_8),
                  "result", "processed".getBytes(StandardCharsets.UTF_8)));
    }
}