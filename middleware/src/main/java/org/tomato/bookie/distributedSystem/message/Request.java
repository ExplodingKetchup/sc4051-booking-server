package org.tomato.bookie.distributedSystem.message;

import lombok.Getter;
import org.ketchup.bookie.common.enums.SerializableDataType;
import org.ketchup.bookie.common.enums.SerializableEnum;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.BinarySerializable;
import org.ketchup.bookie.common.util.SerializeUtils;

import java.util.*;

@Getter
public final class Request implements BinarySerializable {

    @Getter
    public enum Operation implements SerializableEnum {
        // Core operations
        QUERY_AVAILABILITY(0),   // Idempotent
        BOOK_FACILITY(1),        // Non-idempotent
        CHANGE_BOOKING(2),       // Non-idempotent
        MONITOR_FACILITY(3),     // Idempotent
        
        // Additional operations
        LIST_FACILITIES(4),      // Idempotent
        EXTEND_BOOKING(5),       // Non-idempotent

        UNKNOWN(-1);

        private final int value;

        Operation(int value) {
            this.value = value;
        }

        public static Operation fromValue(Integer value) {
            return switch (value) {
                case 0 -> QUERY_AVAILABILITY;
                case 1 -> BOOK_FACILITY;
                case 2 -> CHANGE_BOOKING;
                case 3 -> MONITOR_FACILITY;
                case 4 -> LIST_FACILITIES;
                case 5 -> EXTEND_BOOKING;
                default -> UNKNOWN;
            };
        }
    }

    // Getters
    // Request Format
    private UUID requestId;
    private Operation operation;
    private Map<String, byte[]> parameters;
    private boolean idempotent;     // NOT SERIALIZED!!!

    public Request() {
    }

    public Request(Operation operation, Map<String, byte[]> parameters) {
        this.requestId = UUID.randomUUID();
        this.operation = operation;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
        this.idempotent = determineIdempotence(operation);
    }

    @Override
    public byte[] toBytes() throws SerializationException {
        byte[] uuidAsBytes = SerializeUtils.uuidToBytes(requestId);
        byte[] requestIdKey = SerializeUtils.serializeString("requestId");
        byte[] requestIdValue = SerializeUtils.serializeBytes(uuidAsBytes);
        byte[] operationKey = SerializeUtils.serializeString("operation");
        byte[] operationValue = SerializeUtils.serializeEnum(operation);
        byte[] parametersKey = SerializeUtils.serializeString("parameters");
        byte[] parametersValue = SerializeUtils.serializeMap(parameters, SerializableDataType.BYTES);
        return SerializeUtils.wrapHeader(SerializeUtils.concatBytes(requestIdKey, requestIdValue, operationKey, operationValue, parametersKey, parametersValue), SerializableDataType.OBJECT, 3);
    }

    @Override
    public void fromBytes(byte[] bytes) throws SerializationException {
        int objectSize = SerializeUtils.readHeader(bytes, SerializableDataType.OBJECT);
        if (objectSize != 3) {
            throw new SerializationException("[fromBytes] Number of fields of serialized bytes does not match Request class");
        }
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);

        // requestId
        bytes = SerializeUtils.verifyObjectField(bytes, "requestId");
        int requestIdValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.BYTES);
        UUID requestIdValue = SerializeUtils.uuidFromBytes(SerializeUtils.deserializeBytes(Arrays.copyOfRange(bytes, 0, requestIdValueLen + 4)));
        bytes = Arrays.copyOfRange(bytes, requestIdValueLen + 4, bytes.length);
        requestId = requestIdValue;

        // operation
        bytes = SerializeUtils.verifyObjectField(bytes, "operation");
        int operationValueLen = SerializeUtils.readHeader(bytes, SerializableDataType.ENUM);
        Operation operationValue = SerializeUtils.deserializeEnum(Arrays.copyOfRange(bytes, 0, operationValueLen + 4), Operation.class);
        bytes = Arrays.copyOfRange(bytes, operationValueLen + 4, bytes.length);
        operation = operationValue;

        // parameters
        bytes = SerializeUtils.verifyObjectField(bytes, "parameters");
        Map<String, Object> parametersValue = SerializeUtils.deserializeMap(bytes, SerializableDataType.BYTES);
        parameters = new HashMap<>();
        parametersValue.forEach((key, value) -> parameters.put(key, (byte[]) value));

        // idempotent
        idempotent = determineIdempotence(operation);
    }

    private boolean determineIdempotence(Operation op) {
        return op == Operation.LIST_FACILITIES || 
               op == Operation.QUERY_AVAILABILITY ||
               op == Operation.MONITOR_FACILITY;
    }

}