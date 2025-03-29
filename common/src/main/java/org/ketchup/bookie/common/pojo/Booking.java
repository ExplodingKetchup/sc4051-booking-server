package org.ketchup.bookie.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking implements BinarySerializable {
    public static final Booking NULL_INSTANCE = new Booking(-1, -1, -1, -1, -1);

    private int bookingId;
    private int userId;
    private int facilityId;
    private long bookingTimeStart;
    private int bookingTimeSlots;   // 1 slot = 30 mins

    public static Booking newInstanceFromBytes(byte[] bytes) {
        Booking newInstance = new Booking();
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
