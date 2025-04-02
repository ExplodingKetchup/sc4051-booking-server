package org.ketchup.bookie.client.service;

import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.Availability;
import org.ketchup.bookie.common.util.SerializeUtils;
import org.tomato.bookie.distributedSystem.message.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.UUID;

public class MonitorListener extends Thread {
    private final DatagramSocket socket;
    private final long endTime;
    private final UUID requestId;
    private volatile boolean running = true;
    
    public MonitorListener(DatagramSocket socket, int durationMinutes, UUID requestId) {
        this.socket = socket;
        this.endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        this.requestId = requestId;
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        try {
            socket.setSoTimeout(1000); // 1 second timeout for easier thread termination
            
            System.out.println("Monitor started. Waiting for updates...");
            
            while (running && System.currentTimeMillis() < endTime) {
                try {
                    socket.receive(packet);
                    
                    // Process the received data
                    Response response = new Response();
                    response.fromBytes(Arrays.copyOf(packet.getData(), packet.getLength()));
                    
                    // Verify this is a callback for our monitoring request
                    if (response.getRequestId().equals(requestId)) {
                        // Extract availability information
                        byte[] availabilityBytes = response.getData().get("availability");
                        if (availabilityBytes != null) {
                            Availability availability = new Availability();
                            availability.fromBytes(availabilityBytes);
                            displayAvailabilityUpdate(availability);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // This is expected, just retry
                } catch (SerializationException e) {
                    System.err.println("Failed to deserialize callback response: " + e.getMessage());
                }
            }
            
            System.out.println("Monitoring completed.");
        } catch (IOException e) {
            System.err.println("Error in monitoring listener: " + e.getMessage());
        }
    }
    
    private void displayAvailabilityUpdate(Availability availability) {
        System.out.println("\n=== FACILITY UPDATE RECEIVED ===");
        System.out.println("Facility ID: " + availability.getFacilityId());
        
        // Format and display the booking status
        System.out.println("Current booking status:");
        for (int day = 0; day < 7; day++) {
            System.out.println("  Day " + (day + 1) + ":");
            for (int hour = 0; hour < 24; hour++) {
                StringBuilder hourStatus = new StringBuilder();
                hourStatus.append("    ").append(String.format("%02d", hour)).append(":00 - ");
                
                // Show status for each hour (simplified view)
                boolean hourBooked = false;
                for (int minute = 0; minute < 60; minute++) {
                    int index = (day * 24 * 60) + (hour * 60) + minute;
                    if (index < availability.getBooked().size() && availability.getBooked().get(index)) {
                        hourBooked = true;
                        break;
                    }
                }
                
                hourStatus.append(hourBooked ? "Booked" : "Available");
                System.out.println(hourStatus);
            }
        }
        System.out.println("===============================\n");
    }
    
    public void stopListening() {
        running = false;
    }
}
