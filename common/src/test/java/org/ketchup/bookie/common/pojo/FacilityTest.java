package org.ketchup.bookie.common.pojo;

import org.junit.jupiter.api.Test;
import org.ketchup.bookie.common.enums.FacilityType;
import org.ketchup.bookie.common.exception.SerializationException;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

public class FacilityTest {
    @Test
    void facilitySerializationTest() {
        Facility facility = new Facility(692807, "playground", FacilityType.BADMINTON_COURT);
        try {
            byte[] serializedFacility = facility.toBytes();
            System.out.println("serialized: " + HexFormat.of().formatHex(serializedFacility));
            Facility facility1 = new Facility();
            facility1.fromBytes(serializedFacility);
            System.out.println("Deserialized: " + facility1);
            assertEquals(facility, facility1);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
