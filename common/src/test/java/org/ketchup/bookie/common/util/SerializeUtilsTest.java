package org.ketchup.bookie.common.util;

import org.ketchup.bookie.common.enums.FacilityType;
import org.ketchup.bookie.common.enums.SerializableDataType;
import org.ketchup.bookie.common.exception.SerializationException;

import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SerializeUtilsTest {

    @org.junit.jupiter.api.Test
    void serializeBool() {
        boolean testValue = true;
        try {
            byte[] testValueSerialized = SerializeUtils.serializeBool(testValue);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));
            boolean testValueDeserialized = SerializeUtils.deserializeBool(testValueSerialized);
            System.out.println("Deserialized: " + testValueDeserialized);
            assertEquals(testValue, testValueDeserialized);
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeInt() {
        int testValue = 27681;
        try {
            byte[] testValueSerialized = SerializeUtils.serializeInt(testValue);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));

            int testValueDeserialized = SerializeUtils.deserializeInt(testValueSerialized);
            System.out.println("Deserialized: " + testValueDeserialized);
            assertEquals(testValue, testValueDeserialized);
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeLong() {
        long testValue = 8463968491L;
        try {
            byte[] testValueSerialized = SerializeUtils.serializeLong(testValue);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));
            long testValueDeserialized = SerializeUtils.deserializeLong(testValueSerialized);
            System.out.println("Deserialized: " + testValueDeserialized);
            assertEquals(testValue, testValueDeserialized);
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeString() {
        String testValue = "Mama mia!";
        try {
            byte[] testValueSerialized = SerializeUtils.serializeString(testValue);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));
            String testValueDeserialized = SerializeUtils.deserializeString(testValueSerialized);
            System.out.println("Deserialized: " + testValueDeserialized);
            assertEquals(testValue, testValueDeserialized);
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeEnum() {
        FacilityType testValue = FacilityType.STUDY_POD;
        try {
            byte[] testValueSerialized = SerializeUtils.serializeEnum(testValue);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));
            FacilityType testValueDeserialized = SerializeUtils.deserializeEnum(testValueSerialized, FacilityType.class);
            System.out.println("Deserialized: " + testValueDeserialized);
            assertEquals(testValue, testValueDeserialized);
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeBytes() {
        byte[] testValue = {43, 7, 13, 90, 23, -32, 52, 48};
        try {
            byte[] testValueSerialized = SerializeUtils.serializeBytes(testValue);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));
            byte[] testValueDeserialized = SerializeUtils.deserializeBytes(testValueSerialized);
            System.out.println("Deserialized: " + HexFormat.of().formatHex(testValueDeserialized));
            assertArrayEquals(testValue, testValueDeserialized);
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeMap() {
        Map<String, byte[]> testValue = Map.of("ki1", new byte[] {0, 34, 53, 23, 53, 87}, "ki5", new byte[] {110, -59, 111, 27}, "wrnwp", new byte[] {-21, -21, 52, -61, -89, -41});
        try {
            byte[] testValueSerialized = SerializeUtils.serializeMap(testValue, SerializableDataType.BYTES);
            System.out.println("Serialized: " + HexFormat.of().formatHex(testValueSerialized));
            Map<String, Object> testValueDeserialized = SerializeUtils.deserializeMap(testValueSerialized, SerializableDataType.BYTES);
            System.out.println("Deserialized: " + testValueDeserialized);
            for (String key : testValue.keySet()) {
                assertTrue(testValueDeserialized.containsKey(key));
                assertArrayEquals(testValue.get(key), (byte[]) testValueDeserialized.get(key));
            }
        } catch (SerializationException se) {
            System.out.println("ERROR!!! " + se);
        }
    }

    @org.junit.jupiter.api.Test
    void serializeList() {

    }

    @org.junit.jupiter.api.Test
    void serializeObject() {
    }

    @org.junit.jupiter.api.Test
    void deserializeBool() {
    }

    @org.junit.jupiter.api.Test
    void deserializeInt() {
    }

    @org.junit.jupiter.api.Test
    void deserializeLong() {
    }

    @org.junit.jupiter.api.Test
    void deserializeString() {
    }

    @org.junit.jupiter.api.Test
    void deserializeEnum() {
    }

    @org.junit.jupiter.api.Test
    void deserializeBytes() {
    }

    @org.junit.jupiter.api.Test
    void deserializeMap() {
    }

    @org.junit.jupiter.api.Test
    void deserializeMapEnumValue() {
    }

    @org.junit.jupiter.api.Test
    void deserializeMapObjectValue() {
    }

    @org.junit.jupiter.api.Test
    void deserializeList() {
    }

    @org.junit.jupiter.api.Test
    void deserializeListEnumValue() {
    }

    @org.junit.jupiter.api.Test
    void deserializeListObjectValue() {
    }

    @org.junit.jupiter.api.Test
    void deserializeObject() {
    }

    @org.junit.jupiter.api.Test
    void readHeader() {
    }

    @org.junit.jupiter.api.Test
    void wrapHeader() {
    }

    @org.junit.jupiter.api.Test
    void uuidToBytes() {
        try {
            UUID testValue = UUID.randomUUID();
            System.out.println("original: " + testValue);
            byte[] testValueAsBytes = SerializeUtils.uuidToBytes(testValue);
            System.out.println("byte: " + HexFormat.of().formatHex(testValueAsBytes));
            UUID testValue1 = SerializeUtils.uuidFromBytes(testValueAsBytes);
            System.out.println("converted: " + testValue1);
            assertEquals(testValue, testValue1);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    @org.junit.jupiter.api.Test
    void uuidFromBytes() {
    }

    @org.junit.jupiter.api.Test
    void verifyObjectField() {
    }

    @org.junit.jupiter.api.Test
    void concatBytes() {
    }
}