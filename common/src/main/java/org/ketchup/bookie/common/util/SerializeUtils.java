package org.ketchup.bookie.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ketchup.bookie.common.enums.SerializableDataType;
import org.ketchup.bookie.common.enums.SerializableEnum;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.BinarySerializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class SerializeUtils {

    /**
     * Maximum size of a serialized object. Size of an object is the number of bytes (primitive) or number of key:value pair (Map/Object) or number of members (List)
     * (equals to the maximum 6-bit unsigned integer)
     */
    public static final int MAX_SIZE = 16777215;

    /**
     * Serialize boolean values
     * @param boolValue
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeBool(boolean boolValue) throws SerializationException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (boolValue ? 1 : 0);
        return wrapHeader(bytes, SerializableDataType.BOOLEAN, 1);
    }

    /**
     * Serialize int values
     * @param intValue
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeInt(int intValue) throws SerializationException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        buffer.putInt(intValue);
        return wrapHeader(buffer.array(), SerializableDataType.INT, 4);
    }

    /**
     * Serialize long values
     * @param longValue
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeLong(long longValue) throws SerializationException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        buffer.putLong(longValue);
        return wrapHeader(buffer.array(), SerializableDataType.LONG, 8);
    }

    /**
     * Serialize String values
     * @param stringValue
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeString(String stringValue) throws SerializationException {
        // Size check
        if (stringValue.length() > MAX_SIZE) throw new SerializationException("[serializeString] Input string to serializer is larger than maximum size");
        return wrapHeader(stringValue.getBytes(StandardCharsets.UTF_8), SerializableDataType.STRING, stringValue.length());
    }

    /**
     * Serialize Enum values
     * @param enumValue
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeEnum(SerializableEnum enumValue) throws SerializationException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        buffer.putInt(enumValue.getValue());
        return wrapHeader(buffer.array(), SerializableDataType.ENUM, 4);
    }

    /**
     * Serialize byte[] values
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeBytes(byte[] bytes) throws SerializationException {
        return wrapHeader(bytes, SerializableDataType.BYTES, bytes.length);
    }

    /**
     * Serialize Map values
     * @param map
     * @param valueType Type of the map's value
     * @return
     * @param <T>
     * @throws SerializationException
     */
    public static <T> byte[] serializeMap(Map<String, T> map, SerializableDataType valueType) throws SerializationException {
        if (map.size() > MAX_SIZE) throw new SerializationException("[serializeMap] Input map to serializer is larger than maximum size");
        byte[] contentBytes = new byte[0];
        for (String key : map.keySet()) {
            // Process key
            byte[] keyBytes = serializeString(key);

            // Process value
            T value = map.get(key);
            byte[] valueBytes = switch (valueType) {
                case BOOLEAN -> {
                    if (value instanceof Boolean) {
                        yield serializeBool((Boolean) value);
                    } else {
                        throw new SerializationException("[serializeMap] Provided data type of value does not match actual type");
                    }
                }
                case INT -> {
                    if (value instanceof Integer) {
                        yield serializeInt((Integer) value);
                    } else {
                        throw new SerializationException("[serializeMap] Provided data type of value does not match actual type");
                    }
                }
                case LONG -> {
                    if (value instanceof Long) {
                        yield serializeLong((Long) value);
                    } else {
                        throw new SerializationException("[serializeMap] Provided data type of value does not match actual type");
                    }
                }
                case STRING -> {
                    if (value instanceof String) {
                        yield serializeString((String) value);
                    } else {
                        throw new SerializationException("[serializeMap] Provided data type of value does not match actual type");
                    }
                }
                case ENUM -> {
                    if (value instanceof SerializableEnum) {
                        yield serializeEnum((SerializableEnum) value);
                    } else {
                        throw new SerializationException("[serializeMap] Provided data type of value does not match actual type");
                    }
                }
                case BYTES -> {
                    if (value instanceof byte[]) {
                        yield serializeBytes((byte[]) value);
                    } else {
                        throw new SerializationException("[serializeMap] Provided data type of value does not match actual type");
                    }
                }
                case MAP -> throw new SerializationException("[serializeMap] Value of a Map cannot be a Map");
                case LIST -> throw new SerializationException("[serializeMap] Value of a Map cannot be a List");
                case OBJECT -> {
                    if (value instanceof BinarySerializable) {
                        yield ((BinarySerializable) value).toBytes();
                    } else {
                        throw new SerializationException("[serializeMap] Value of a Map cannot be an object not implementing BinarySerializable");
                    }
                }
            };

            contentBytes = concatBytes(contentBytes, keyBytes, valueBytes);
        }
        return wrapHeader(contentBytes, SerializableDataType.MAP, map.size());
    }

    /**
     * Serialize List values
     * @param list
     * @param memberType Type of the List's members
     * @return
     * @param <T>
     * @throws SerializationException
     */
    public static <T> byte[] serializeList(List<T> list, SerializableDataType memberType) throws SerializationException {
        if (list.size() > MAX_SIZE) throw new SerializationException("[serializeList] Input list to serializer is larger than maximum size");
        byte[] result = new byte[0];
        for (T member : list) {
            byte[] memberBytes = switch (memberType) {
                case BOOLEAN -> {
                    if (member instanceof Boolean) {
                        yield serializeBool((Boolean) member);
                    } else {
                        throw new SerializationException("[serializeList] Provided data type of member does not match actual type");
                    }
                }
                case INT -> {
                    if (member instanceof Integer) {
                        yield serializeInt((Integer) member);
                    } else {
                        throw new SerializationException("[serializeList] Provided data type of member does not match actual type");
                    }
                }
                case LONG -> {
                    if (member instanceof Long) {
                        yield serializeLong((Long) member);
                    } else {
                        throw new SerializationException("[serializeList] Provided data type of member does not match actual type");
                    }
                }
                case STRING -> {
                    if (member instanceof String) {
                        yield serializeString((String) member);
                    } else {
                        throw new SerializationException("[serializeList] Provided data type of member does not match actual type");
                    }
                }
                case ENUM -> {
                    if (member instanceof SerializableEnum) {
                        yield serializeEnum((SerializableEnum) member);
                    } else {
                        throw new SerializationException("[serializeList] Provided data type of member does not match actual type");
                    }
                }
                case BYTES -> {
                    if (member instanceof byte[]) {
                        yield serializeBytes((byte[]) member);
                    } else {
                        throw new SerializationException("[serializeList] Provided data type of member does not match actual type");
                    }
                }
                case MAP -> throw new SerializationException("[serializeList] Member of a List cannot be a Map");
                case LIST -> throw new SerializationException("[serializeList] Member of a List cannot be a List");
                case OBJECT -> {
                    if (member instanceof BinarySerializable) {
                        yield ((BinarySerializable) member).toBytes();
                    } else {
                        throw new SerializationException("[serializeList] Member of a List cannot be an object not implementing BinarySerializable");
                    }
                }
            };
            result = concatBytes(result, memberBytes);
        }
        return wrapHeader(result, SerializableDataType.LIST, list.size());
    }

    /**
     * Serialize Object values (not recommended to use this approach). This method is a proxy which invokes {@link BinarySerializable#toBytes()}.
     * @param object
     * @return
     * @throws SerializationException
     */
    public static byte[] serializeObject(BinarySerializable object) throws SerializationException {
        return object.toBytes();
    }

    /**
     * Deserialize boolean values
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static boolean deserializeBool(byte[] bytes) throws SerializationException {
        if (readHeader(bytes, SerializableDataType.BOOLEAN) != bytes.length-4 || bytes.length != 5) {
            log.error("[deserializeBool] Invalid byte array for bool");
            throw new SerializationException("[deserializeBool] Invalid byte array for bool");
        }
        return bytes[4] != 0;
    }

    /**
     * Deserialize int values
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static int deserializeInt(byte[] bytes) throws SerializationException {
        if (readHeader(bytes, SerializableDataType.INT) != bytes.length-4 || bytes.length != 8) {
            log.error("[deserializeInt] Invalid byte array for int");
            throw new SerializationException("[deserializeInt] Invalid byte array for int");
        }
        ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4, bytes.length));
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        return buffer.getInt();
    }

    /**
     * Deserialize long values
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static long deserializeLong(byte[] bytes) throws SerializationException {
        if (readHeader(bytes, SerializableDataType.LONG) != bytes.length-4 || bytes.length != 12) {
            log.error("[deserializeLong] Invalid byte array for long");
            throw new SerializationException("[deserializeLong] Invalid byte array for long");
        }
        ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4, bytes.length));
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        return buffer.getLong();
    }

    /**
     * Deserialize String values
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static String deserializeString(byte[] bytes) throws SerializationException {
        int strlen = readHeader(bytes, SerializableDataType.STRING);
        if (strlen < 0 || strlen > MAX_SIZE || bytes.length-4 != strlen) {
            log.error("[deserializeString] Invalid byte array for String");
            throw new SerializationException("[deserializeString] Invalid byte array for String");
        }
        return new String(Arrays.copyOfRange(bytes, 4, bytes.length), StandardCharsets.UTF_8);
    }

    /**
     * Deserialize enum values
     * @param bytes
     * @param enumType Class of the enum
     * @return
     * @param <E>
     * @throws SerializationException
     */
    public static <E extends SerializableEnum> E deserializeEnum(byte[] bytes, Class<E> enumType) throws SerializationException {
        if (readHeader(bytes, SerializableDataType.ENUM) != bytes.length-4 || bytes.length != 8) {
            log.error("[deserializeEnum] Invalid byte array for enum");
            throw new SerializationException("[deserializeEnum] Invalid byte array for enum");
        }
        ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4, bytes.length));
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        int enumValue = buffer.getInt();
        try {
            Method fromValueMethod = enumType.getMethod("fromValue", Integer.class);
            return (E) fromValueMethod.invoke(null, enumValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("[deserializeEnum] Failed to access fromValue() method of enum type {}", enumType, e);
        }
        return null;
    }

    /**
     * Deserialize byte[] values
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static byte[] deserializeBytes(byte[] bytes) throws SerializationException {
        int bytesLen = readHeader(bytes, SerializableDataType.BYTES);
        if (bytesLen != bytes.length-4 || bytesLen < 0 || bytesLen > MAX_SIZE) {
            log.error("[deserializeBytes] Invalid byte array for Bytes");
            throw new SerializationException("[deserializeBytes] Invalid byte array for Bytes");
        }
        return Arrays.copyOfRange(bytes, 4, bytes.length);
    }

    /**
     * Deserialize Map values whose values are boolean, int, long, string, and byte[]
     * @param bytes
     * @param valueType Type of the map's values
     * @return
     * @throws SerializationException
     */
    public static Map<String, Object> deserializeMap(byte[] bytes, SerializableDataType valueType) throws SerializationException {
        int mapSize = readHeader(bytes, SerializableDataType.MAP);
        if (mapSize < 0 || mapSize > MAX_SIZE) {
            log.error("[deserializeMap] Invalid length of map value");
            throw new SerializationException("[deserializeMap] Invalid length of map value");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        Map<String, Object> result = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            // Read key
            int keyLen = readHeader(bytes, SerializableDataType.STRING);
            String key = deserializeString(Arrays.copyOfRange(bytes, 0, keyLen + 4));
            bytes = Arrays.copyOfRange(bytes, keyLen + 4, bytes.length);

            // Read value
            int valueLen = readHeader(bytes, valueType);
            byte[] valueBytes = Arrays.copyOfRange(bytes, 0, valueLen + 4);
            Object value = switch (valueType) {
                case BOOLEAN -> deserializeBool(valueBytes);
                case INT -> deserializeInt(valueBytes);
                case LONG -> deserializeLong(valueBytes);
                case STRING -> deserializeString(valueBytes);
                case ENUM -> throw new SerializationException("[deserializeMap] Wrong method, please use deserializeMapEnumValue instead");
                case BYTES -> deserializeBytes(valueBytes);
                case MAP -> throw new SerializationException("[deserializeMap] Map value cannot be a Map");
                case LIST -> throw new SerializationException("[deserializeMap] Map value cannot be a List");
                case OBJECT -> throw new SerializationException("[deserializeMap] Wrong method, please use deserializeMapObjectValue instead");
            };
            bytes = Arrays.copyOfRange(bytes, valueLen + 4, bytes.length);

            result.put(key, value);
        }
        return result;
    }

    /**
     * Deserialize Map values whose values are enums
     * @param bytes
     * @param enumClass Class of the map values
     * @return
     * @param <E>
     * @throws SerializationException
     */
    public static <E extends SerializableEnum> Map<String, Object> deserializeMapEnumValue(byte[] bytes, Class<E> enumClass) throws SerializationException {
        int mapSize = readHeader(bytes, SerializableDataType.MAP);
        if (mapSize < 0 || mapSize > MAX_SIZE) {
            log.error("[deserializeMapEnumValue] Invalid length of map value");
            throw new SerializationException("[deserializeMapEnumValue] Invalid length of map value");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        Map<String, Object> result = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            // Read key
            int keyLen = readHeader(bytes, SerializableDataType.STRING);
            String key = deserializeString(Arrays.copyOfRange(bytes, 0, keyLen + 4));
            bytes = Arrays.copyOfRange(bytes, keyLen + 4, bytes.length);

            // Read value
            int valueLen = readHeader(bytes, SerializableDataType.ENUM);
            Object value = deserializeEnum(Arrays.copyOfRange(bytes, 0, valueLen + 4), enumClass);
            bytes = Arrays.copyOfRange(bytes, valueLen + 4, bytes.length);

            result.put(key, value);
        }
        return result;
    }

    /**
     * Deserialize Map whose values are objects
     * @param bytes
     * @param objectClass Class of the map values
     * @return
     * @param <T>
     * @throws SerializationException
     */
    public static <T extends BinarySerializable> Map<String, Object> deserializeMapObjectValue(byte[] bytes, Class<T> objectClass) throws SerializationException {
        int mapSize = readHeader(bytes, SerializableDataType.MAP);
        if (mapSize < 0 || mapSize > MAX_SIZE) {
            log.error("[deserializeMapObjectValue] Invalid length of map value");
            throw new SerializationException("[deserializeMapObjectValue] Invalid length of map value");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        Map<String, Object> result = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            // Read key
            int keyLen = readHeader(bytes, SerializableDataType.STRING);
            String key = deserializeString(Arrays.copyOfRange(bytes, 0, keyLen + 4));
            bytes = Arrays.copyOfRange(bytes, keyLen + 4, bytes.length);

            // Read value
            int valueLen = readHeader(bytes, SerializableDataType.OBJECT);
            try {
                BinarySerializable value = objectClass.getDeclaredConstructor((Class<?>) null).newInstance();
                value.fromBytes(Arrays.copyOfRange(bytes, 0, valueLen + 4));
                bytes = Arrays.copyOfRange(bytes, valueLen + 4, bytes.length);

                result.put(key, value);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("[deserializeMapObjectValue] Failed to invoke the 0-arg constructor of the specified object");
                throw new SerializationException("[deserializeMapObjectValue] Failed to invoke the 0-arg constructor of the specified object");
            }
        }
        return result;
    }

    /**
     * Deserialize a List whose members are boolean, int, long, String, or byte[]
     * @param bytes
     * @param memberType Type of the List members
     * @return
     * @throws SerializationException
     */
    public static List<Object> deserializeList(byte[] bytes, SerializableDataType memberType) throws SerializationException {
        int listSize = readHeader(bytes, SerializableDataType.LIST);
        if (listSize < 0 || listSize > MAX_SIZE) {
            log.error("[deserializeList] Invalid length of list value");
            throw new SerializationException("[deserializeList] Invalid length of list value");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        List<Object> result = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            // Read member
            int memberLen = readHeader(bytes, memberType);
            byte[] memberBytes = Arrays.copyOfRange(bytes, 0, memberLen + 4);
            Object member = switch (memberType) {
                case BOOLEAN -> deserializeBool(memberBytes);
                case INT -> deserializeInt(memberBytes);
                case LONG -> deserializeLong(memberBytes);
                case STRING -> deserializeString(memberBytes);
                case ENUM -> throw new SerializationException("[deserializeList] Wrong method, please use deserializeListEnumValue instead");
                case BYTES -> deserializeBytes(memberBytes);
                case MAP -> throw new SerializationException("[deserializeList] List member cannot be a Map");
                case LIST -> throw new SerializationException("[deserializeList] List member cannot be a List");
                case OBJECT -> throw new SerializationException("[deserializeList] Wrong method, please use deserializeListObjectValue instead");
            };
            bytes = Arrays.copyOfRange(bytes, memberLen + 4, bytes.length);

            result.add(member);
        }
        return result;
    }

    /**
     * Deserialize a List whose members are enums
     * @param bytes
     * @param enumClass the class of the list members
     * @return
     * @param <E>
     * @throws SerializationException
     */
    public static <E extends SerializableEnum> List<Object> deserializeListEnumValue(byte[] bytes, Class<E> enumClass) throws SerializationException {
        int listSize = readHeader(bytes, SerializableDataType.LIST);
        if (listSize < 0 || listSize > MAX_SIZE) {
            log.error("[deserializeListEnumValue] Invalid length of list value");
            throw new SerializationException("[deserializeListEnumValue] Invalid length of list value");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        List<Object> result = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            // Read value
            int memberLen = readHeader(bytes, SerializableDataType.ENUM);
            Object member = deserializeEnum(Arrays.copyOfRange(bytes, 0, memberLen + 4), enumClass);
            bytes = Arrays.copyOfRange(bytes, memberLen + 4, bytes.length);

            result.add(member);
        }
        return result;
    }

    /**
     * Deserialize a List whose members are objects.
     * @param bytes
     * @param objectClass the class of the members.
     * @return
     * @param <T>
     * @throws SerializationException
     */
    public static <T extends BinarySerializable> List<Object> deserializeListObjectValue(byte[] bytes, Class<T> objectClass) throws SerializationException {
        int listSize = readHeader(bytes, SerializableDataType.MAP);
        if (listSize < 0 || listSize > MAX_SIZE) {
            log.error("[deserializeListEnumValue] Invalid length of list value");
            throw new SerializationException("[deserializeListEnumValue] Invalid length of list value");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        List<Object> result = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            // Read value
            int memberLen = readHeader(bytes, SerializableDataType.OBJECT);
            try {
                BinarySerializable member = objectClass.getDeclaredConstructor((Class<?>) null).newInstance();
                member.fromBytes(Arrays.copyOfRange(bytes, 0, memberLen + 4));
                bytes = Arrays.copyOfRange(bytes, memberLen + 4, bytes.length);

                result.add(member);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("[deserializeMapObjectValue] Failed to invoke the 0-arg constructor of the specified object");
                throw new SerializationException("[deserializeMapObjectValue] Failed to invoke the 0-arg constructor of the specified object");
            }
        }
        return result;
    }

    /**
     * Deserialize objects (Not recommended approach, please directly use {@link BinarySerializable#fromBytes(byte[])} instead).
     * This method is a proxy to invoke {@link BinarySerializable#fromBytes(byte[])} and creating a new instance with the help of reflection.
     * @param bytes
     * @param objectClass
     * @return
     * @param <T>
     * @throws SerializationException
     */
    public static <T extends BinarySerializable> T deserializeObject(byte[] bytes, Class<T> objectClass) throws SerializationException {
        try {
            BinarySerializable object = objectClass.getDeclaredConstructor((Class<?>) null).newInstance();
            object.fromBytes(bytes);
            return (T) object;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("[deserializeObject] Failed to invoke the 0-arg constructor of the specified object");
            throw new SerializationException("[deserializeObject] Failed to invoke the 0-arg constructor of the specified object");
        }
    }

    /**
     * Read the header of a value type, verify the header's correctness and return the size of the object.
     * Does not check sizes.
     * @param bytes
     * @param dataType
     * @return
     * @throws SerializationException
     */
    public static int readHeader(byte[] bytes, SerializableDataType dataType) throws SerializationException {
        if (Objects.isNull(bytes) || bytes.length < 4) {
            throw new SerializationException("The serialized object must at least be 4 bytes in size");
        }
        byte[] headerBytes = Arrays.copyOfRange(bytes, 0, 4);
        // Verify if the data type matches
        if (headerBytes[0] != dataType.getSerializedValue()) {
            throw new SerializationException("Wrong header for type " + dataType.name());
        }
        // Read the size bytes
        byte[] sizeBytes = Arrays.copyOfRange(headerBytes, 1, 5);
        ByteBuffer buffer = ByteBuffer.wrap(sizeBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        return buffer.getInt();
    }

    /**
     * Add a 4-byte header to the start of the serialized values.
     * @param bytes serialized values without header
     * @param dataType type of the serialized values
     * @param size size of the serialized values ( = number of bytes for primitive) ( = number of key:value pairs for Map/Object) ( = number of members for List)
     * @return
     * @throws SerializationException
     */
    public static byte[] wrapHeader(byte[] bytes, SerializableDataType dataType, int size) throws SerializationException {
        if (size > MAX_SIZE) {
            throw new SerializationException("The size of the object to be serialized is too large");
        }
        byte[] dataTypeByte = {(byte) dataType.getSerializedValue()};
        ByteBuffer sizeBytesBuffer = ByteBuffer.allocate(4);
        sizeBytesBuffer.order(ByteOrder.LITTLE_ENDIAN); // Set byte order to little-endian
        sizeBytesBuffer.putInt(size);
        byte[] sizeBytes = Arrays.copyOfRange(sizeBytesBuffer.array(), 0, 3);

        return concatBytes(dataTypeByte, sizeBytes, bytes);
    }

    /**
     * Convert UUID to byte[] (not serialize, therefore does not include header)
     * @param uuid
     * @return
     */
    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16); // A UUID is 16 bytes
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Convert byte[] to UUID (not deserialize, therefore will not check for header)
     * @param bytes
     * @return
     * @throws SerializationException
     */
    public static UUID uuidFromBytes(byte[] bytes) throws SerializationException {
        if (bytes.length != 16) {
            throw new SerializationException("[uuidFromBytes] Invalid byte array length for UUID conversion.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Verify if the first key found in bytes matches the field string. Return a new byte[] equals to bytes minus the field key.
     * @param bytes
     * @param field
     * @return
     * @throws SerializationException
     */
    public static byte[] verifyObjectField(byte[] bytes, String field) throws SerializationException {
        int keyLen = SerializeUtils.readHeader(bytes, SerializableDataType.STRING);
        if (keyLen != field.length()) {
            throw new SerializationException("[fromBytes] Fields [" + field + "] not exist");
        }
        String key = SerializeUtils.deserializeString(Arrays.copyOfRange(bytes, 0, keyLen + 4));
        if (!StringUtils.equals(key, field)) {
            throw new SerializationException("[fromBytes] Fields [" + field + "] not exist");
        }
        return Arrays.copyOfRange(bytes, keyLen + 4, bytes.length);
    }

    /**
     * Concatenate multiple byte[] to a single byte[].
     * @param bytes
     * @return
     */
    public static byte[] concatBytes(byte[]... bytes) {
        int totalLength = 0;
        for (byte[] aByteArray : bytes) {
            totalLength += aByteArray.length;
        }
        byte[] result = new byte[totalLength];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        for (byte[] aByteArray : bytes) {
            buffer.put(aByteArray);
        }
        return buffer.array();
    }
}
