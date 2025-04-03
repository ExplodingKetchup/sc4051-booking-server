package org.ketchup.bookie.client.service;

import org.ketchup.bookie.client.config.ClientConfig;
import org.ketchup.bookie.common.exception.SerializationException;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientService {
    private final ClientConfig config;
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int serverPort;
    
    // For at-most-once semantics
    private final Map<UUID, Response> responseCache = new ConcurrentHashMap<>();
    
    // For monitoring
    private MonitorListener monitorListener;
    
    public ClientService(ClientConfig config) {
        this.config = config;
        this.serverPort = config.getServerPort();
        
        try {
            this.socket = new DatagramSocket(55000);
            this.serverAddress = InetAddress.getByName(config.getServerIP());
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException("Failed to initialize client service", e);
        }
    }
    
    public Response sendRequest(Request request) throws IOException {
        // For at-most-once semantics, check cache first
        if (config.isAtMostOnceEnabled() && responseCache.containsKey(request.getRequestId())) {
            return responseCache.get(request.getRequestId());
        }
        
        byte[] requestBytes;
        try {
            requestBytes = request.toBytes();
        } catch (SerializationException e) {
            throw new IOException("Failed to serialize request", e);
        }
        
        DatagramPacket packet = new DatagramPacket(
                requestBytes, requestBytes.length, serverAddress, serverPort);
        
        // Set timeout for the socket
        socket.setSoTimeout(3000); // 3 seconds
        
        int retries = 0;
        int maxRetries = 3;
        
        while (retries < maxRetries) {
            try {
                socket.send(packet);
                
                // Prepare to receive response
                byte[] responseBuffer = new byte[8192]; // Adjust size as needed
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                
                // Receive response (blocks until response is received or timeout)
                socket.receive(responsePacket);
                
                // Deserialize response
                Response response = new Response();
                try {
                    response.fromBytes(Arrays.copyOf(responsePacket.getData(), responsePacket.getLength()));
                    
                    // For at-most-once semantics, cache the response
                    if (config.isAtMostOnceEnabled()) {
                        responseCache.put(request.getRequestId(), response);
                    }
                    
                    return response;
                } catch (SerializationException e) {
                    throw new IOException("Failed to deserialize response", e);
                }
            } catch (SocketTimeoutException e) {
                retries++;
                System.out.println("Request timed out, retry " + retries + "/" + maxRetries);
            }
        }
        
        throw new IOException("Failed to get response after " + maxRetries + " attempts");
    }
    
    public void startMonitorListener(int duration, UUID requestId) {
        monitorListener = new MonitorListener(socket, duration, requestId);
        monitorListener.start();
    }
    
    public void stopMonitorListener() {
        if (monitorListener != null) {
            monitorListener.stopListening();
        }
    }
    
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
