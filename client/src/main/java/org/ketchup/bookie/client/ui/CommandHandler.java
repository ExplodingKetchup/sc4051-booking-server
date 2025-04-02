package org.ketchup.bookie.client.ui;

import org.ketchup.bookie.client.service.ClientService;
import org.ketchup.bookie.client.service.RequestBuilder;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.Availability;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.common.util.SerializeUtils;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CommandHandler {
    private final ClientService clientService;
    private final RequestBuilder requestBuilder;
    private final Scanner scanner;
    
    public CommandHandler(ClientService clientService) {
        this.clientService = clientService;
        this.requestBuilder = new RequestBuilder();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Handles the query availability command
     */
    public void handleQueryAvailability() throws IOException {
        System.out.println("\n=== Query Facility Availability ===");
        
        try {
            System.out.print("Enter facility ID: ");
            int facilityId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter start time (minutes from start of week): ");
            int startTime = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter end time (minutes from start of week): ");
            int endTime = Integer.parseInt(scanner.nextLine().trim());
            
            Request request = requestBuilder.buildQueryAvailabilityRequest(facilityId, startTime, endTime);
            Response response = clientService.sendRequest(request);
            
            if (response.isStatus()) {
                System.out.println("Facility is available during the requested period.");
            } else {
                System.out.println("Facility is not available. Error: " + extractErrorMessage(response));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values.");
        }
    }
    
    /**
     * Handles the book facility command
     */
    public void handleBookFacility() throws IOException {
        System.out.println("\n=== Book Facility ===");
        
        try {
            System.out.print("Enter facility ID: ");
            int facilityId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter booking start time (e.g., 'Monday 14:30' or minutes from start of week): ");
            String startTimeInput = scanner.nextLine().trim();
            int startTime = parseTimeInput(startTimeInput);
            
            System.out.print("Enter booking end time (e.g., 'Monday 16:00' or minutes from start of week): ");
            String endTimeInput = scanner.nextLine().trim();
            int endTime = parseTimeInput(endTimeInput);
            
            Request request = requestBuilder.buildBookFacilityRequest(facilityId, startTime, endTime);
            Response response = clientService.sendRequest(request);
            
            if (response.isStatus()) {
                int bookingId = SerializeUtils.deserializeInt(response.getData().get("bookingId"));
                System.out.println("Booking successful! Your booking ID is: " + bookingId);
                System.out.println("Please keep this ID for future reference.");
            } else {
                System.out.println("Booking failed. Error: " + extractErrorMessage(response));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values or use the format 'Day HH:MM'.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SerializationException e) {
            System.out.println("Error processing response: " + e.getMessage());
        }
    }
    
    /**
     * Parses time input in either format:
     * - Integer (minutes from Monday 00:00)
     * - String like "Tuesday 14:30"
     * 
     * @param input Time input string
     * @return Minutes from Monday 00:00
     */
    private int parseTimeInput(String input) {
        // Try parsing as integer first
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            // Not a number, try parsing as day and time
            String[] parts = input.trim().split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid time format. Use 'Day HH:MM' or minutes from Monday 00:00.");
            }
            
            String day = parts[0].toLowerCase();
            String time = parts[1];
            
            String[] timeParts = time.split(":");
            if (timeParts.length != 2) {
                throw new IllegalArgumentException("Invalid time format. Use 'HH:MM' format for time.");
            }
            
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                throw new IllegalArgumentException("Invalid time values. Hours must be 0-23, minutes must be 0-59.");
            }
            
            int dayOffset;
            switch (day.toLowerCase()) {
                case "monday": dayOffset = 0; break;
                case "tuesday": dayOffset = 1; break;
                case "wednesday": dayOffset = 2; break;
                case "thursday": dayOffset = 3; break;
                case "friday": dayOffset = 4; break;
                case "saturday": dayOffset = 5; break;
                case "sunday": dayOffset = 6; break;
                default: throw new IllegalArgumentException("Invalid day name. Use Monday-Sunday.");
            }
            
            return (dayOffset * 24 * 60) + (hours * 60) + minutes;
        }
    }
    
    /**
     * Handles the change booking command
     */
    public void handleChangeBooking() throws IOException {
        System.out.println("\n=== Change Booking ===");
        
        try {
            System.out.print("Enter booking ID: ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter time offset (minutes, can be positive or negative): ");
            int offsetTime = Integer.parseInt(scanner.nextLine().trim());
            
            Request request = requestBuilder.buildChangeBookingRequest(bookingId, offsetTime);
            Response response = clientService.sendRequest(request);
            
            if (response.isStatus()) {
                System.out.println("Booking changed successfully.");
            } else {
                System.out.println("Failed to change booking. Error: " + extractErrorMessage(response));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values.");
        }
    }
    
    /**
     * Handles the monitor facility command
     */
    public void handleMonitorFacility() throws IOException {
        System.out.println("\n=== Monitor Facility ===");
        
        try {
            System.out.print("Enter facility ID: ");
            int facilityId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter monitoring duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine().trim());
            
            Request request = requestBuilder.buildMonitorFacilityRequest(facilityId, duration);
            Response response = clientService.sendRequest(request);
            
            if (response.isStatus()) {
                System.out.println("Monitoring started for " + duration + " minutes.");
                clientService.startMonitorListener(duration, request.getRequestId());
                
                System.out.println("Press Enter to stop monitoring early...");
                scanner.nextLine();
                clientService.stopMonitorListener();
            } else {
                System.out.println("Failed to start monitoring. Error: " + extractErrorMessage(response));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values.");
        }
    }
    
    /**
     * Handles the list facilities command
     */
    public void handleListFacilities() throws IOException {
        System.out.println("\n=== List All Facilities ===");
        
        Request request = requestBuilder.buildListFacilitiesRequest();
        Response response = clientService.sendRequest(request);
        
        if (response.isStatus()) {
            List<Facility> facilities = new ArrayList<>();
            Map<String, byte[]> data = response.getData();
            
            for (int i = 0; ; i++) {
                byte[] facilityBytes = data.get("facility-" + i);
                if (facilityBytes == null) break;
                
                try {
                    Facility facility = new Facility();
                    facility.fromBytes(facilityBytes);
                    facilities.add(facility);
                } catch (SerializationException e) {
                    System.out.println("Error deserializing facility: " + e.getMessage());
                }
            }
            
            if (facilities.isEmpty()) {
                System.out.println("No facilities found.");
            } else {
                System.out.println("Available facilities:");
                for (Facility facility : facilities) {
                    System.out.println("ID: " + facility.getId() + ", Name: " + facility.getName() + 
                                       ", Type: " + facility.getType());
                }
            }
        } else {
            System.out.println("Failed to retrieve facilities. Error: " + extractErrorMessage(response));
        }
    }
    
    /**
     * Handles the extend booking command
     */
    public void handleExtendBooking() throws IOException {
        System.out.println("\n=== Extend Booking ===");
        
        try {
            System.out.print("Enter booking ID: ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter extension time (minutes): ");
            int offsetTime = Integer.parseInt(scanner.nextLine().trim());
            
            Request request = requestBuilder.buildExtendBookingRequest(bookingId, offsetTime);
            Response response = clientService.sendRequest(request);
            
            if (response.isStatus()) {
                System.out.println("Booking extended successfully.");
            } else {
                System.out.println("Failed to extend booking. Error: " + extractErrorMessage(response));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values.");
        }
    }
    
    /**
     * Helper method to extract error messages from responses
     */
    private String extractErrorMessage(Response response) {
        if (response.getData().containsKey("error")) {
            try {
                return new String(response.getData().get("error"));
            } catch (Exception e) {
                return "Unknown error";
            }
        }
        return "Unknown error";
    }
    
    /**
     * Helper method to convert minutes to readable time format
     */
    private String formatTime(int minutes) {
        int days = minutes / (24 * 60);
        int hours = (minutes % (24 * 60)) / 60;
        int mins = minutes % 60;
        
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return dayNames[days] + " " + String.format("%02d:%02d", hours, mins);
    }
}
