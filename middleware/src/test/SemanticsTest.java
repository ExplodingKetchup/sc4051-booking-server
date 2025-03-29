package src.test;

import java.util.Map;

import src.main.distributedSystem.faultolerance.Deduplicator;
import src.main.distributedSystem.message.*;
import src.main.distributedSystem.semantics.*;

public class SemanticsTest {
    public static void main(String[] args) {
        // Test At-Most-Once
        Deduplicator deduplicator = new Deduplicator();
        InvocationSemantic atMostOnce = new AtMostOnce(deduplicator);
        
        Request request = new Request(
            Request.Operation.EXTEND_BOOKING,
            "Conference Room 1",
            Map.of("confirmationId", "123", "minutes", 30)
        );
        
        System.out.println("First attempt: " + atMostOnce.execute(request).getStatus());
        System.out.println("Duplicate attempt: " + atMostOnce.execute(request).getStatus());

        // Test At-Least-Once
        InvocationSemantic atLeastOnce = new AtLeastOnce();
        System.out.println("Final result: " + atLeastOnce.execute(request).getStatus());
    }
}
