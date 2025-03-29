package org.tomato.bookie.distributedSystem.semantics;

import java.util.Map;

import org.tomato.bookie.distributedSystem.faultolerance.Deduplicator;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public class AtMostOnce implements InvocationSemantic {
    private final Deduplicator deduplicator;

    public AtMostOnce(Deduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    @Override
    public Response execute(Request request) {
        if (deduplicator.isDuplicate(request.getRequestId())) {
            return Response.error(request.getRequestId(), "Duplicate request detected");
        }

        deduplicator.recordRequest(request.getRequestId());
        return simulateProcessing(request);
    }
        
    private Response simulateProcessing(Request request) {
        // Simulated processing - would be real server call in actual implementation
        return Response.success(request.getRequestId(), 
            Map.of("operation", request.getOperation().name(),
                  "result", "processed"));
    }
}