package org.ketchup.bookie.common.pojo;

public interface BinarySerializable {
    byte[] toBytes();
    void fromBytes(byte[] bytes);
}
