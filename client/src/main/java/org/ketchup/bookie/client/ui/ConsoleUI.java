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

public class ConsoleUI {
    private final ClientService clientService;
    private final RequestBuilder requestBuilder;
    private final Scanner scanner;
    
    public ConsoleUI(ClientService clientService) {
        this.clientService = clientService;
        this.requestBuilder = new RequestBuilder();
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        boolean running = true;
        
        System.out.println("Welcome to the Facility Booking System");
        
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        queryAvailability();
                        break;
                    case 2:
                        bookFacility();
                        break;
                    case 3:
                        changeBooking();
                        break;
                    case 4:
                        monitorFacility();
                        break;
                    case 5:
                        listFacilities();
                        break;
                    case 6:
                        extendBooking();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        System.out.println("Thank you for using the Facility Booking System.");
        clientService.close();
    }
    
    private void printMenu() {
        System.out.println("\n===== Facility Booking System =====");
        System.out.println("1. Query Facility Availability");
        System.out.println("2. Book Facility");
        System.out.println("3. Change Booking");
        System.out.println("4. Monitor Facility");
        System.out.println("5. List All Facilities");
        System.out.println("6. Extend Booking");
        System.out.println("0. Exit");
    }
    
    private void queryAvailability() throws IOException {
        System.out.println("\n=== Query Facility Availability ===");
        
        System.out.print("Enter facility ID: ");
        int facilityId = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter start time (minutes from Monday 00:00): ");
        int checkTimeStart = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter end time (minutes from Monday 00:00): ");
        int checkTimeEnd = Integer.parseInt(scanner.nextLine().trim());
        
        Request request = requestBuilder.buildQueryAvailabilityRequest(facilityId, checkTimeStart, checkTimeEnd);
        Response response = clientService.sendRequest(request);
        
        if (response.isStatus()) {
            System.out.println("The facility is available during the requested time period.");
        } else {
            System.out.println("Error: " + extractErrorMessage(response));
        }
    }
    
    private void bookFacility() throws IOException {
        System.out.println("\n=== Book Facility ===");
        
        System.out.print("Enter facility ID: ");
        int facilityId = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter booking start time (minutes from Monday 00:00): ");
        int bookingStartTime = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter booking end time (minutes from Monday 00:00): ");
        int bookingEndTime = Integer.parseInt(scanner.nextLine().trim());
        
        Request request = requestBuilder.buildBookFacilityRequest(facilityId, bookingStartTime, bookingEndTime);
        Response response = clientService.sendRequest(request);
        
        if (response.isStatus()) {
            try {
                int bookingId = SerializeUtils.deserializeInt(response.getData().get("bookingId"));
                System.out.println("Booking successful! Booking ID: " + bookingId);
            } catch (SerializationException e) {
                System.out.println("Booking successful, but could not read booking ID.");
            }
        } else {
            System.out.println("Booking failed. Error: " + extractErrorMessage(response));
        }
    }
    
    private void changeBooking() throws IOException {
        System.out.println("\n=== Change Booking ===");
        
        System.out.print("Enter booking ID: ");
        int bookingId = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter time offset (positive or negative minutes): ");
        int offsetTime = Integer.parseInt(scanner.nextLine().trim());
        
        Request request = requestBuilder.buildChangeBookingRequest(bookingId, offsetTime);
        Response response = clientService.sendRequest(request);
        
        if (response.isStatus()) {
            System.out.println("Booking changed successfully.");
        } else {
            System.out.println("Failed to change booking. Error: " + extractErrorMessage(response));
        }
    }
    
    private void monitorFacility() throws IOException {
        System.out.println("\n=== Monitor Facility ===");
        
        System.out.print("Enter facility ID: ");
        int facilityId = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter monitoring duration (minutes): ");
        int duration = Integer.parseInt(scanner.nextLine().trim());
        
        Request request = requestBuilder.buildMonitorFacilityRequest(facilityId, duration);
        Response response = clientService.sendRequest(request);
        
        if (response.isStatus()) {
            System.out.println("Monitoring started for " + duration + " minutes.");
            clientService.startMonitorListener(duration, request.getRequestId());
            
            // Wait for monitoring to complete
            System.out.println("Press Enter to stop monitoring early...");
            scanner.nextLine();
            clientService.stopMonitorListener();
        } else {
            System.out.println("Failed to start monitoring. Error: " + extractErrorMessage(response));
        }
    }
    
    private void listFacilities() throws IOException {
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
    
    private void extendBooking() throws IOException {
        System.out.println("\n=== Extend Booking ===");
        
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
    }
    
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
}
