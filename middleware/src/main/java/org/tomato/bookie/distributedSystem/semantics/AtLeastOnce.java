package org.tomato.bookie.distributedSystem.semantics;

import java.util.Map;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public class AtLeastOnce implements InvocationSemantic {
    private static final int MAX_RETRIES = 3;
    private static final double FAILURE_RATE = 0.3; // 30% failure chance

    @Override
    public Response execute(Request request) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Response response = attemptProcess(request);
            
            if (response.getStatus() == Response.Status.SUCCESS) {
                if (!request.isIdempotent() && attempt > 0) {
                    System.out.println("[WARNING] Potential duplicate execution of non-idempotent operation");
                }
                return response;
            }
            
            try {
                Thread.sleep(100 * (attempt + 1)); // Exponential backoff
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Response.error(request.getRequestId(), "Operation interrupted");
            }
        }
        return Response.error(request.getRequestId(), "Max retries exceeded");
    }

    private Response attemptProcess(Request request) {
        // Simulate random failures
        if (Math.random() < FAILURE_RATE) {
            return Response.error(request.getRequestId(), "Simulated processing failure");
        }
        return Response.success(request.getRequestId(), 
            Map.of("operation", request.getOperation().name(),
                  "attempts", "succeeded after retries"));
    }
}
