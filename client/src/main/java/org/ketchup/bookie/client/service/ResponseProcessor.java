package org.ketchup.bookie.client.service;

import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.Availability;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.common.util.SerializeUtils;
import org.tomato.bookie.distributedSystem.message.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes responses from the server, extracting and formatting data
 * for client consumption.
 */
public class ResponseProcessor {
    
    /**
     * Checks if a response indicates success
     * 
     * @param response The response to check
     * @return true if the response indicates a successful operation
     */
    public boolean isSuccess(Response response) {
        return response != null && response.isStatus() && !hasError(response);
    }
    
    /**
     * Checks if a response contains an error message
     * 
     * @param response The response to check
     * @return true if the response contains an error field
     */
    public boolean hasError(Response response) {
        return response != null && response.getData().containsKey("error");
    }
    
    /**
     * Extracts an error message from a response
     * 
     * @param response The response to process
     * @return The error message or "Unknown error" if not found
     */
    public String extractErrorMessage(Response response) {
        if (response == null || !response.getData().containsKey("error")) {
            return "Unknown error";
        }
        
        try {
            byte[] errorBytes = response.getData().get("error");
            if (errorBytes.length == 0) {
                return "Unknown error";
            }
            return SerializeUtils.deserializeString(errorBytes);
        } catch (Exception e) {
            return "Error processing error message: " + e.getMessage();
        }
    }
    
    /**
     * Extracts a booking ID from a booking response
     * 
     * @param response The booking response
     * @return The booking ID
     * @throws SerializationException if deserialization fails
     */
    public int extractBookingId(Response response) throws SerializationException {
        if (response == null || !response.getData().containsKey("bookingId")) {
            throw new IllegalArgumentException("Response does not contain a booking ID");
        }
        
        return SerializeUtils.deserializeInt(response.getData().get("bookingId"));
    }
    
    /**
     * Extracts a list of facilities from a list facilities response
     * 
     * @param response The list facilities response
     * @return A list of facilities
     */
    public List<Facility> extractFacilities(Response response) {
        List<Facility> facilities = new ArrayList<>();
        
        if (response == null || !response.isStatus()) {
            return facilities;
        }
        
        Map<String, byte[]> data = response.getData();
        for (int i = 0; ; i++) {
            byte[] facilityBytes = data.get("facility-" + i);
            if (facilityBytes == null) break;
            
            try {
                Facility facility = new Facility();
                facility.fromBytes(facilityBytes);
                facilities.add(facility);
            } catch (SerializationException e) {
                System.err.println("Error deserializing facility: " + e.getMessage());
            }
        }
        
        return facilities;
    }
    
    /**
     * Extracts availability information from a monitor callback response
     * 
     * @param response The callback response
     * @return An Availability object or null if not found
     */
    public Availability extractAvailability(Response response) {
        if (response == null || !response.isStatus() || 
                !response.getData().containsKey("availability")) {
            return null;
        }
        
        try {
            byte[] availabilityBytes = response.getData().get("availability");
            Availability availability = new Availability();
            availability.fromBytes(availabilityBytes);
            return availability;
        } catch (SerializationException e) {
            System.err.println("Error deserializing availability: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Formats availability data for display
     * 
     * @param availability The availability data
     * @return A formatted string representation
     */
    public String formatAvailability(Availability availability) {
        if (availability == null) {
            return "No availability data";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Facility ID: ").append(availability.getFacilityId()).append("\n");
        result.append("Availability:\n");
        
        List<Boolean> bookings = availability.getBooked();
        for (int day = 0; day < 7; day++) {
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            result.append("  ").append(days[day]).append(":\n");
            
            for (int hour = 0; hour < 24; hour++) {
                StringBuilder hourSummary = new StringBuilder();
                hourSummary.append("    ").append(String.format("%02d", hour)).append(":00 - ");
                
                // Check each minute in this hour
                boolean isHourBooked = false;
                for (int minute = 0; minute < 60; minute++) {
                    int index = (day * 24 * 60) + (hour * 60) + minute;
                    if (index < bookings.size() && bookings.get(index)) {
                        isHourBooked = true;
                        break;
                    }
                }
                
                hourSummary.append(isHourBooked ? "Booked" : "Available");
                result.append(hourSummary).append("\n");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Helper method to convert minutes from week start to readable time format
     * 
     * @param minutes Minutes from start of week (Monday 00:00)
     * @return A formatted time string
     */
    public String formatTime(int minutes) {
        int days = minutes / (24 * 60);
        int hours = (minutes % (24 * 60)) / 60;
        int mins = minutes % 60;
        
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return dayNames[days] + " " + String.format("%02d:%02d", hours, mins);
    }
}
