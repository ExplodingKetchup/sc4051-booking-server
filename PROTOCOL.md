# Message Format and Protocols

## Transport
UDP

## Operation Parameters and Return Values

Note that boolean-like responses are not listed here. All boolean-like responses can be obtained by checking the status of `Response`.

`QUERY_AVAILABILITY`
- Request:
  - "facilityId" : `int`
  - "checkTimeStart" : `long`
  - "checkTimeEnd" : `long`
- Response: None

`BOOK_FACILITY`
- Request: 
  - "booking" : `Booking`
- Response: 
  - "bookingId" : `int`

`CHANGE_BOOKING`
- Request:
  - "bookingId" : `int`
  - "offsetTimeSlots": `int`
- Response: None

## Message Serialization

### Primitive Types
- `boolean`: 1-byte byte array
- `int`: 4-byte little-endian byte array
- `long`: 8-byte little-endian byte array
- `string`: variable-length byte array UTF-8 encoded
- `enum`: Each enum is assigned a numeric value (`int`). Value of `enum` on the wire is the same as `int`

Each value of the primitive types are preceded by a Header.

### Map / Object type
Each Map/Object is serialized to a list of key:value pairs consecutively, preceded by a header. For example:

```
<map_header><key_1><value_1><key_2><value_2>...<key_n><value_n>
```

Where `key` is always a String and `value` could be a serialized primitive value or an Object. `value` cannot be a Map or List.

Each `key` represents a property of the serialized object or a key of a Map. Currently, only Map<String,...> is supported.

### Lists
A list of objects is serialized as follows:
```
<list_header><value_1><value_2>...<value_n>
```
Where each `value` could be a serialized Primitive or Object. `value` cannot be a Map or List.

### Header
Each value of a type is serialized with a 4-byte headers.
```
00        00        00        00
byte[0]   byte[1]   byte[2]   byte[3]
```
`byte[0]`: Type of data 
`byte[1] -> byte[3]`: Size (little endian, unsigned int)
- If it is a primitive header, that is the number of bytes of the object (little endian).
- If it is a Map/Object header, that is the number of key:value pairs in the Map.
- If it is a List header, that is the number of elements in the list.

Type of data:
```
01 -> boolean
02 -> int
03 -> long
04 -> string
05 -> enum
06 -> byte string
07 -> Map/Object
08 -> List
09 -> Object
```

### Notes on implementation
While Map and Object share the same format, for Object, each serialized object must implement `org.ketchup.bookie.common.pojo.BinarySerializable` interface, which includes specifying how to serialize/deserialize.

In theory, we can perform Object serialization/deserialization similar to a Map. However, using Java's `reflection` is overly complicated.

All Enums must implement `org.ketchup.bookie.common.enums.SerializableEnum`, which requires each enum value to be associated with a unique number.

For serialization, the `getValue()` method will be used. On deserialization, although not specified in the `SerializableEnum` interface, each enum must implement a static `fromValue(Integer)` method to convert integers to enum values.