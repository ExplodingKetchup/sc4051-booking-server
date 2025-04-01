package org.ketchup.bookie.common.pojo;

import org.junit.jupiter.api.Test;
import org.ketchup.bookie.common.exception.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AvailabilityTest {
    @Test
    void availabilitySerializationTest() {
        Random random = new Random(System.currentTimeMillis());
        List<Boolean> availList = new ArrayList<>();
        for (int i = 0; i < 7 * 24 * 60; i++) {
            availList.add(random.nextBoolean());
        }
        Availability testValue = new Availability();
        testValue.setFacilityId(955957);
        testValue.setBooked(availList);
        try {
            byte[] serializedTestValue = testValue.toBytes();
            System.out.println("serialized size: " + serializedTestValue.length);
            Availability testValue1 = new Availability();
            testValue1.fromBytes(serializedTestValue);
            System.out.println("deserialized: " + testValue1);
            assertEquals(testValue.getFacilityId(), testValue1.getFacilityId());
            assertEquals(7*24*60, testValue1.getBooked().size());
            for (int i = 0; i < 7*24*60; i++) {
                assertEquals(testValue.getBooked().get(i), testValue1.getBooked().get(i));
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
