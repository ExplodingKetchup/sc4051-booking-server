package org.ketchup.bookie.client.service;

import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.util.SerializeUtils;
import org.tomato.bookie.distributedSystem.message.Request;

import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
    
    public Request buildQueryAvailabilityRequest(int facilityId, int checkTimeStart, int checkTimeEnd) {
        Map<String, byte[]> parameters = new HashMap<>();
        try {
            parameters.put("facilityId", SerializeUtils.serializeInt(facilityId));
            parameters.put("checkTimeStart", SerializeUtils.serializeInt(checkTimeStart));
            parameters.put("checkTimeEnd", SerializeUtils.serializeInt(checkTimeEnd));
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to serialize request parameters", e);
        }
        
        return new Request(Request.Operation.QUERY_AVAILABILITY, parameters);
    }
    
    public Request buildBookFacilityRequest(int facilityId, int bookingStartTime, int bookingEndTime) {
        Map<String, byte[]> parameters = new HashMap<>();
        try {
            parameters.put("facilityId", SerializeUtils.serializeInt(facilityId));
            parameters.put("bookingStartTime", SerializeUtils.serializeInt(bookingStartTime));
            parameters.put("bookingEndTime", SerializeUtils.serializeInt(bookingEndTime));
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to serialize request parameters", e);
        }
        
        return new Request(Request.Operation.BOOK_FACILITY, parameters);
    }
    
    public Request buildChangeBookingRequest(int bookingId, int offsetTime) {
        Map<String, byte[]> parameters = new HashMap<>();
        try {
            parameters.put("bookingId", SerializeUtils.serializeInt(bookingId));
            parameters.put("offsetTime", SerializeUtils.serializeInt(offsetTime));
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to serialize request parameters", e);
        }
        
        return new Request(Request.Operation.CHANGE_BOOKING, parameters);
    }
    
    public Request buildMonitorFacilityRequest(int facilityId, int duration) {
        Map<String, byte[]> parameters = new HashMap<>();
        try {
            parameters.put("facilityId", SerializeUtils.serializeInt(facilityId));
            parameters.put("duration", SerializeUtils.serializeInt(duration));
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to serialize request parameters", e);
        }
        
        return new Request(Request.Operation.MONITOR_FACILITY, parameters);
    }
    
    public Request buildListFacilitiesRequest() {
        // LIST_FACILITIES doesn't require any parameters
        return new Request(Request.Operation.LIST_FACILITIES, new HashMap<>());
    }
    
    public Request buildExtendBookingRequest(int bookingId, int offsetTime) {
        Map<String, byte[]> parameters = new HashMap<>();
        try {
            parameters.put("bookingId", SerializeUtils.serializeInt(bookingId));
            parameters.put("offsetTime", SerializeUtils.serializeInt(offsetTime));
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to serialize request parameters", e);
        }
        
        return new Request(Request.Operation.EXTEND_BOOKING, parameters);
    }
}
