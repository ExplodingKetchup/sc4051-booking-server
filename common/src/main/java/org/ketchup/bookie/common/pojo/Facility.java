package org.ketchup.bookie.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ketchup.bookie.common.enums.FacilityType;
import org.ketchup.bookie.common.enums.SerializableDataType;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.util.SerializeUtils;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Facility implements BinarySerializable{
    public static final Facility NULL_INSTANCE = new Facility(-1, "", FacilityType.UNKNOWN);

    private int id;
    private String name;
    private FacilityType type;

    public static Facility newInstanceFromBytes(byte[] bytes) throws SerializationException {
        Facility newInstance = new Facility();
        newInstance.fromBytes(bytes);
        return newInstance;
    }

    @Override
    public byte[] toBytes() throws SerializationException {
        byte[] idKey = SerializeUtils.serializeString("id");
        byte[] idValue = SerializeUtils.serializeInt(id);
        byte[] nameKey = SerializeUtils.serializeString("name");
        byte[] nameValue = SerializeUtils.serializeString(name);
        byte[] typeKey = SerializeUtils.serializeString("type");
        byte[] typeValue = SerializeUtils.serializeEnum(type);
        return SerializeUtils.wrapHeader(SerializeUtils.concatBytes(idKey, idValue, nameKey, nameValue, typeKey, typeValue), SerializableDataType.OBJECT, 3);
    }

    @Override
    public void fromBytes(byte[] bytes) throws SerializationException {
        int objectSize = SerializeUtils.readHeader(bytes, SerializableDataType.OBJECT);
        if (objectSize != 2) {
            throw new SerializationException("[fromBytes] Number of fields of serialized bytes does not match Facility class");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);

        bytes = SerializeUtils.verifyObjectField(bytes, "id");
        int idValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.INT);
        id = SerializeUtils.deserializeInt(Arrays.copyOfRange(bytes, 0, idValueLen + 4));
        bytes = Arrays.copyOfRange(bytes, idValueLen + 4, bytes.length);

        bytes = SerializeUtils.verifyObjectField(bytes, "name");
        int nameValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.STRING);
        name = SerializeUtils.deserializeString(Arrays.copyOfRange(bytes, 0, nameValueLen + 4));
        bytes = Arrays.copyOfRange(bytes, nameValueLen + 4, bytes.length);

        bytes = SerializeUtils.verifyObjectField(bytes, "type");
        int typeValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.ENUM);
        type = SerializeUtils.deserializeEnum(Arrays.copyOfRange(bytes, 0, typeValueLen + 4), FacilityType.class);
    }
}
