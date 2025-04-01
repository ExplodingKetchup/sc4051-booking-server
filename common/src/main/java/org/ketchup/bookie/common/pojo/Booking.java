package org.ketchup.bookie.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ketchup.bookie.common.exception.SerializationException;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking implements BinarySerializable {
    public static final Booking NULL_INSTANCE = new Booking(-1, -1, -1, -1);

    private int bookingId;
    private int facilityId;
    private int bookingStartTime;   // Inclusive
    private int bookingEndTime;     // Exclusive

    public static Booking newInstanceFromBytes(byte[] bytes) {
        try {
            Booking newInstance = new Booking();
            newInstance.fromBytes(bytes);
            return newInstance;
        } catch (SerializationException se) {
            return NULL_INSTANCE;
        }
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public void fromBytes(byte[] bytes) throws SerializationException {
        if (Objects.isNull(bytes) || bytes.length == 0) {
            throw new SerializationException("[fromBytes] Byte array is null or empty");
        }
    }
}
