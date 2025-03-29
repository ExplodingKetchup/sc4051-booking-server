package org.tomato.bookie.distributedSystem.message;

import java.util.*;

public final class Request {
    public enum Operation {
        // Core operations
        QUERY_AVAILABILITY,   // Idempotent
        BOOK_FACILITY,        // Non-idempotent
        CHANGE_BOOKING,       // Non-idempotent
        MONITOR_FACILITY,     // Idempotent
        
        // Additional operations
        LIST_FACILITIES,      // Idempotent
        EXTEND_BOOKING        // Non-idempotent
    }

    // Request Format
    private final UUID requestId;
    private final Operation operation;
    private final String facilityName;
    private final Map<String, Object> parameters;
    private final boolean idempotent;

    public Request(Operation operation, String facilityName, Map<String, Object> parameters) {
        this.requestId = UUID.randomUUID();
        this.operation = operation;
        this.facilityName = facilityName;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
        this.idempotent = determineIdempotence(operation);
    }

    private boolean determineIdempotence(Operation op) {
        return op == Operation.LIST_FACILITIES || 
               op == Operation.QUERY_AVAILABILITY ||
               op == Operation.MONITOR_FACILITY;
    }

    // Getters
    public UUID getRequestId() { return requestId; }
    public Operation getOperation() { return operation; }
    public String getFacilityName() { return facilityName; }
    public Map<String, Object> getParameters() { return parameters; }
    public boolean isIdempotent() { return idempotent; }
}