package org.tomato.bookie.distributedSystem.message;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.ketchup.bookie.common.enums.SerializableDataType;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.BinarySerializable;
import org.ketchup.bookie.common.util.SerializeUtils;

import java.util.*;

@Getter
public final class Response implements BinarySerializable {

    // Getters
    private UUID requestId;
    private boolean status;
    private Map<String, byte[]> data;

    public Response() {
    }

    public Response(UUID requestId, boolean status, Map<String, byte[]> data) {
        this.requestId = requestId;
        this.status = status;
        this.data = Collections.unmodifiableMap(new HashMap<>(data));
    }

    // Factory methods
    public static Response success(UUID requestId) {
        return new Response(requestId, true, Collections.emptyMap());
    }

    public static Response success(UUID requestId, Map<String, byte[]> data) {
        return new Response(requestId, true, data);
    }

    public static Response error(UUID requestId) {
        return new Response(requestId, false, Map.of("error", new byte[0]));
    }

    public static Response error(UUID requestId, String message) throws SerializationException {
        return new Response(requestId, false, Map.of("error", SerializeUtils.serializeString(message)));
    }

    public static Response applicationError(UUID requestId) {
        return new Response(requestId, true, Map.of("error", new byte[0]));
    }

    public static Response applicationError(UUID requestId, String message) throws SerializationException {
        return new Response(requestId, true, Map.of("error", SerializeUtils.serializeString(message)));
    }

    @Override
    public byte[] toBytes() throws SerializationException {
        byte[] uuidAsBytes = SerializeUtils.uuidToBytes(requestId);
        byte[] requestIdKeyBytes = SerializeUtils.serializeString("requestId");
        byte[] requestIdValueBytes = SerializeUtils.serializeBytes(uuidAsBytes);
        byte[] statusKeyBytes = SerializeUtils.serializeString("status");
        byte[] statusValueBytes = SerializeUtils.serializeBool(status);
        byte[] dataKeyBytes = SerializeUtils.serializeString("data");
        byte[] dataValueBytes = SerializeUtils.serializeMap(data, SerializableDataType.BYTES);
        return SerializeUtils.wrapHeader(SerializeUtils.concatBytes(requestIdKeyBytes, requestIdValueBytes, statusKeyBytes, statusValueBytes, dataKeyBytes, dataValueBytes), SerializableDataType.OBJECT, 3);
    }

    @Override
    public void fromBytes(byte[] bytes) throws SerializationException {
        int objectSize = SerializeUtils.readHeader(bytes, SerializableDataType.OBJECT);
        if (objectSize != 3) {
            throw new SerializationException("[fromBytes] Number of fields of serialized bytes does not match Response class");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);

        // requestId
        bytes = SerializeUtils.verifyObjectField(bytes, "requestId");
        int requestIdValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.BYTES);
        UUID requestIdValue = SerializeUtils.uuidFromBytes(SerializeUtils.deserializeBytes(Arrays.copyOfRange(bytes, 0, requestIdValueLen + 4)));
        bytes = Arrays.copyOfRange(bytes, requestIdValueLen + 4, bytes.length);
        requestId = requestIdValue;

        // status
        bytes = SerializeUtils.verifyObjectField(bytes, "status");
        int statusValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.BOOLEAN);
        boolean statusValue = SerializeUtils.deserializeBool(Arrays.copyOfRange(bytes, 0, statusValueLen + 4));
        bytes = Arrays.copyOfRange(bytes, statusValueLen + 4, bytes.length);
        status = statusValue;

        // data
        bytes = SerializeUtils.verifyObjectField(bytes, "data");
        Map<String, Object> dataValue = SerializeUtils.deserializeMap(bytes, SerializableDataType.BYTES);
        dataValue.forEach((key, value) -> data.put(key, (byte[]) value));
    }
}