package org.ketchup.bookie.common.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ketchup.bookie.common.enums.SerializableDataType;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.BinarySerializable;
import org.ketchup.bookie.common.util.SerializeUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class Availability implements BinarySerializable {
    private static final int MINS_IN_WEEK = 7 * 24 * 60;
    private int facilityId;
    private List<Boolean> booked;
    
    private byte[] bookedAsBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(MINS_IN_WEEK / 8);
        int i = 0;
        byte b = 0;
        for (Boolean bookedAtSlot : booked) {
            if (i == 0) {
                b = 0;
            }
            b = (byte) ((b << 1) | (bookedAtSlot ? 1 : 0));
            i = (i + 1) % 8;
            if (i == 0) {
                buffer.put(b);
            }
        }
        return buffer.array();
    }

    private void bookedFromBytes(byte[] bytes) {
        booked = new ArrayList<>(MINS_IN_WEEK);
        for (byte aByte : bytes) {
            while (aByte != 0) {
                booked.add((aByte & (byte) 0x80) != 0);
                aByte = (byte) (aByte << 1);
            }
        }
    }

    @Override
    public byte[] toBytes() throws SerializationException {
        byte[] facilityIdKey = SerializeUtils.serializeString("facilityIdKey");
        byte[] facilityIdValue = SerializeUtils.serializeInt(facilityId);
        byte[] bookedKey = SerializeUtils.serializeString("booked");
        byte[] bookedValue = SerializeUtils.serializeBytes(bookedAsBytes());
        return SerializeUtils.wrapHeader(SerializeUtils.concatBytes(facilityIdKey, facilityIdValue, bookedKey, bookedValue), SerializableDataType.OBJECT, 2);
    }

    @Override
    public void fromBytes(byte[] bytes) throws SerializationException {
        int objectSize = SerializeUtils.readHeader(bytes, SerializableDataType.OBJECT);
        if (objectSize != 2) {
            throw new SerializationException("[fromBytes] Number of fields of serialized bytes does not match Availability class");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);

        // facilityId
        bytes = SerializeUtils.verifyObjectField(bytes, "facilityId");
        int facilityIdValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.INT);
        facilityId = SerializeUtils.deserializeInt(Arrays.copyOfRange(bytes, 0, facilityIdValueLen + 4));
        bytes = Arrays.copyOfRange(bytes, facilityIdValueLen + 4, bytes.length);

        // booked
        bytes = SerializeUtils.verifyObjectField(bytes, "booked");
        int bookedValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.BYTES);
        bookedFromBytes(SerializeUtils.deserializeBytes(Arrays.copyOfRange(bytes, 0, bookedValueLen + 4)));
    }
}
