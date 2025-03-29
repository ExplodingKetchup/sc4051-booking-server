package org.tomato.bookie.distributedSystem.message;

import java.util.*;

public final class Response {
    public enum Status { 
        SUCCESS, 
        ERROR 
    }
    
    private final UUID requestId;
    private final Status status;
    private final Map<String, Object> data;

    public Response(UUID requestId, Status status, Map<String, Object> data) {
        this.requestId = requestId;
        this.status = status;
        this.data = Collections.unmodifiableMap(new HashMap<>(data));
    }

    // Factory methods
    public static Response success(UUID requestId, Map<String, Object> data) {
        return new Response(requestId, Status.SUCCESS, data);
    }

    public static Response error(UUID requestId, String message) {
        return new Response(requestId, Status.ERROR, Map.of("error", message));
    }

    // Getters
    public UUID getRequestId() { return requestId; }
    public Status getStatus() { return status; }
    public Map<String, Object> getData() { return data; }
}