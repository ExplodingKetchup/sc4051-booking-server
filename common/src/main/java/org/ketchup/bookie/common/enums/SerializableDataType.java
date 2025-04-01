package org.ketchup.bookie.common.enums;

import lombok.Getter;

@Getter
public enum SerializableDataType {
    BOOLEAN(1),
    INT(2),
    LONG(3),
    STRING(4),
    ENUM(5),
    BYTES(6),
    MAP(7),
    LIST(8),
    OBJECT(9);

    private final int serializedValue;

    SerializableDataType(int serializedValue) {
        this.serializedValue = serializedValue;
    }
}
