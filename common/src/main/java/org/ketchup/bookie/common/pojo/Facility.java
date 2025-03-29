package org.ketchup.bookie.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ketchup.bookie.common.enums.FacilityType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Facility implements BinarySerializable{
    public static final Facility NULL_INSTANCE = new Facility(-1, "", FacilityType.UNKNOWN);

    private int id;
    private String name;
    private FacilityType type;

    public static Facility newInstanceFromBytes(byte[] bytes) {
        Facility newInstance = new Facility();
        newInstance.fromBytes(bytes);
        return newInstance;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public void fromBytes(byte[] bytes) {

    }
}
