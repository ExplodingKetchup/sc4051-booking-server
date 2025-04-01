package org.tomato.bookie.distributedSystem.message;

import org.junit.jupiter.api.Test;
import org.ketchup.bookie.common.exception.SerializationException;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void responseSerializationTest() {
        Response response = new Response(UUID.randomUUID(), true, Map.of("ki1", new byte[] {0, 34, 53, 23, 53, 87}, "ki5", new byte[] {110, -59, 111, 27}, "wrnwp", new byte[] {-21, -21, 52, -61, -89, -41}));
        try {
            byte[] serialized = response.toBytes();
            Response response1 = new Response();
            response1.fromBytes(serialized);
            System.out.println("deserialized: " + response1);
            assertEquals(response.getRequestId(), response1.getRequestId());
            assertEquals(response.isStatus(), response1.isStatus());
            assertEquals(response.getData().size(), response1.getData().size());
            for (String key : response.getData().keySet()) {
                assertTrue(response1.getData().containsKey(key));
                assertArrayEquals(response.getData().get(key), response1.getData().get(key));
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}