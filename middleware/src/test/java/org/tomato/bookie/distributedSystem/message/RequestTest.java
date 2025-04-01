package org.tomato.bookie.distributedSystem.message;

import org.ketchup.bookie.common.exception.SerializationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @org.junit.jupiter.api.Test
    void requestSerializationTest() {
        Request testValue = new Request(Request.Operation.CHANGE_BOOKING, Map.of("ki1", new byte[] {0, 34, 53, 23, 53, 87}, "ki5", new byte[] {110, -59, 111, 27}, "wrnwp", new byte[] {-21, -21, 52, -61, -89, -41}));
        try {
            byte[] serializedTestValue = testValue.toBytes();
            Request request = new Request();
            request.fromBytes(serializedTestValue);
            System.out.println("deserialized: " + request);
            assertEquals(testValue.getRequestId(), request.getRequestId());
            assertEquals(testValue.getOperation(), request.getOperation());
            assertEquals(testValue.isIdempotent(), request.isIdempotent());
            assertEquals(testValue.getParameters().size(), request.getParameters().size());
            for (String key : testValue.getParameters().keySet()) {
                assertTrue(request.getParameters().containsKey(key));
                assertArrayEquals(testValue.getParameters().get(key), request.getParameters().get(key));
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}