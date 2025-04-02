package org.ketchup.bookie.server.service;

import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.*;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.common.util.SerializeUtils;
import org.ketchup.bookie.server.config.Config;
import org.ketchup.bookie.server.handler.ExceptionHandler;
import org.ketchup.bookie.server.interceptor.RequestInterceptor;
import org.ketchup.bookie.server.interceptor.ResponseInterceptor;
import org.ketchup.bookie.server.repository.FacilityRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tomato.bookie.distributedSystem.faultolerance.Deduplicator;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RequestListener implements InitializingBean {

    private final Config config;
    private final Deduplicator deduplicator;
    private final RequestInterceptor requestInterceptor;
    private final ExceptionHandler exceptionHandler;
    private final ResponseInterceptor responseInterceptor;
    private final BookingManager bookingManager;
    private final FacilityRepository facilityRepository;
    private final AvailabilityMonitoringService availabilityMonitoringService;

    private InetAddress clientAddress;

    private int clientPort;


    public RequestListener(Config config, Deduplicator deduplicator, RequestInterceptor requestInterceptor, ExceptionHandler exceptionHandler, ResponseInterceptor responseInterceptor, BookingManager bookingManager, FacilityRepository facilityRepository, AvailabilityMonitoringService availabilityMonitoringService) {
        this.config = config;
        this.deduplicator = deduplicator;
        this.requestInterceptor = requestInterceptor;
        this.exceptionHandler = exceptionHandler;
        this.responseInterceptor = responseInterceptor;
        this.bookingManager = bookingManager;
        this.facilityRepository = facilityRepository;
        this.availabilityMonitoringService = availabilityMonitoringService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Init stuffs

        // Starts listener
        listenForRequests(config.getPort());
    }

    public void listenForRequests(int port) {
        // Listening on port
        byte[] buffer = new byte[SerializeUtils.MAX_SIZE];

        try (DatagramSocket socket = new DatagramSocket(port)) {
            log.info("Server is listening on port {}", port);

            while (true) {
                // Prepare packet for receiving data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Receive packet (blocking call)

                // Process packet
                byte[] response = handleClientRequest(packet.getData());
                clientAddress = packet.getAddress();
                clientPort = packet.getPort();

                // Send a response back to the client
                DatagramPacket responsePacket = new DatagramPacket(
                        response, response.length, clientAddress, clientPort);
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] handleClientRequest(byte[] requestBytes) {
        // When receiving a request, deserialize it to Request object
        Request request = new Request();
        try {
            request.fromBytes(requestBytes);
        } catch (SerializationException se) {
            log.error("[handleClientRequest] Request deserialization failed", se);
            try {
                return exceptionHandler.handleException(se).toBytes();
            } catch (SerializationException e) {
                throw new RuntimeException("Response serialization failed", e);
            }
        }

        // At-most-once semantic checking
        if (config.isAtMostOnceEnabled()) {
            if (deduplicator.isDuplicate(request.getRequestId())) {
                try {
                    return Response.error(request.getRequestId(), "Duplicate request detected").toBytes();
                } catch (SerializationException se) {
                    log.error("[execute] Error message serialization failed, message: [Duplicate request detected]", se);
                    try {
                        return Response.error(request.getRequestId()).toBytes();
                    } catch (SerializationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            deduplicator.recordRequest(request.getRequestId());
        }

        // Pass Request to interceptors
        request = requestInterceptor.intercept(request);

        // Pass Request object to handler
        Response response;
        try {
            response = handleClientRequest(request);
            response = responseInterceptor.intercept(request, response);
        } catch (Throwable throwable) {
            response = exceptionHandler.handleException(request, throwable);
        }

        try {
            return response.toBytes();
        } catch (SerializationException se) {
            throw new RuntimeException("Response serialization failed", se);
        }
    }

    private Response handleClientRequest(Request request) throws MalformedRequestException, UnavailableFacilityException, UnavailableBookingException, MalformedResponseException {
        return switch (request.getOperation()) {
            case QUERY_AVAILABILITY -> {
                try {
                    int facilityId = SerializeUtils.deserializeInt(request.getParameters().get("facilityId"));
                    int checkTimeStart = SerializeUtils.deserializeInt(request.getParameters().get("checkTimeStart"));
                    int checkTimeEnd = SerializeUtils.deserializeInt(request.getParameters().get("checkTimeEnd"));
                    bookingManager.checkAvailability(facilityId, checkTimeStart, checkTimeEnd);
                    yield Response.success(request.getRequestId());
                } catch (SerializationException se) {
                    throw new MalformedRequestException("[handleClientRequest] Bad params for QUERY_AVAILABILITY", se);
                }
            }
            case BOOK_FACILITY -> {
                try {
                    int facilityId = SerializeUtils.deserializeInt(request.getParameters().get("facilityId"));
                    int bookingStartTime = SerializeUtils.deserializeInt(request.getParameters().get("bookingStartTime"));
                    int bookingEndTime = SerializeUtils.deserializeInt(request.getParameters().get("bookingEndTime"));
                    yield Response.success(
                            request.getRequestId(),
                            Map.of(
                                    "bookingId",
                                    SerializeUtils.serializeInt(bookingManager.addBooking(facilityId, bookingStartTime, bookingEndTime))
                            )
                    );
                } catch (SerializationException se) {
                    throw new MalformedRequestException("[handleClientRequest] Bad params for BOOK_FACILITY", se);
                }
            }
            case CHANGE_BOOKING -> {
                try {
                    int bookingId = SerializeUtils.deserializeInt(request.getParameters().get("bookingId"));
                    int offsetTime = SerializeUtils.deserializeInt(request.getParameters().get("offsetTime"));
                    bookingManager.changeBooking(bookingId, offsetTime);
                    yield Response.success(request.getRequestId());
                } catch (SerializationException se) {
                    throw new MalformedRequestException("[handleClientRequest] Bad params for CHANGE_BOOKING", se);
                }
            }
            case MONITOR_FACILITY -> {
                try {
                    int facilityId = SerializeUtils.deserializeInt(request.getParameters().get("facilityId"));
                    int duration = SerializeUtils.deserializeInt(request.getParameters().get("duration"));
                    availabilityMonitoringService.addToMailingList(request.getRequestId(), clientAddress, clientPort, facilityId, duration);
                    yield Response.success(request.getRequestId());
                } catch (SerializationException se) {
                    throw new MalformedRequestException("[handleClientRequest] Bad params for MONITOR_FACILITY", se);
                }
            }
            case LIST_FACILITIES -> {
                try {
                    List<Facility> allFacilities = facilityRepository.listAllFacilities();
                    Map<String, byte[]> dataMap = new HashMap<>(allFacilities.size());
                    int index = 0;
                    for (Facility facility : allFacilities) {
                        dataMap.put("facility-" + index, facility.toBytes());
                        index++;
                    }
                    yield Response.success(request.getRequestId(), dataMap);
                } catch (SerializationException se) {
                    throw new MalformedResponseException("[handleClientRequest] Failed to construct response for LIST_FACILITIES");
                }
            }
            case EXTEND_BOOKING -> {
                try {
                    int bookingId = SerializeUtils.deserializeInt(request.getParameters().get("bookingId"));
                    int offsetTime = SerializeUtils.deserializeInt(request.getParameters().get("offsetTime"));
                    bookingManager.extendBooking(bookingId, offsetTime);
                    yield Response.success(request.getRequestId());
                } catch (SerializationException se) {
                    throw new MalformedRequestException("[handleClientRequest] Bad params for EXTEND_BOOKING", se);
                }
            }
            case UNKNOWN -> throw new MalformedRequestException("[handleClientRequest] Invalid request operation");
        };
    }
}
