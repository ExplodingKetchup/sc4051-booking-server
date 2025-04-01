package org.ketchup.bookie.common.pojo;

import org.ketchup.bookie.common.exception.SerializationException;

public interface BinarySerializable {
    byte[] toBytes() throws SerializationException;
    void fromBytes(byte[] bytes) throws SerializationException;
}
