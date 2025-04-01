package org.ketchup.bookie.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.exception.UnavailableFacilityException;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.common.pojo.Availability;
import org.ketchup.bookie.server.repository.FacilityRepository;
import org.springframework.stereotype.Service;
import org.tomato.bookie.distributedSystem.message.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

@Service
@Slf4j
public class AvailabilityMonitoringService {

    private final FacilityRepository facilityRepository;

    // { facilityId : List<requestId> }
    private final Map<Integer, Set<UUID>> mailingList;
    // { requestId : { clientAddress : clientPort }
    private final Map<UUID, Pair<InetAddress, Integer>> mailingAddresses;
    // { requestId : expiryTime }
    private final Map<UUID, Long> subscriptionExpiry;

    public AvailabilityMonitoringService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
        mailingList = new HashMap<>();
        mailingAddresses = new HashMap<>();
        subscriptionExpiry = new HashMap<>();
    }

    public void addToMailingList(UUID requestId, InetAddress clientAddress, Integer clientPort, Integer facilityId, Integer monitorDuration) throws UnavailableFacilityException {
        if (facilityRepository.getFacilityById(facilityId).equals(Facility.NULL_INSTANCE)) {
            throw new UnavailableFacilityException();
        }
        if (monitorDuration <= 0) {
            throw new IllegalArgumentException("[addToMailingList] Monitor Duration must be positive");
        }
        mailingList.putIfAbsent(facilityId, new HashSet<>());
        mailingList.get(facilityId).add(requestId);
        mailingAddresses.put(requestId, Pair.of(clientAddress, clientPort));
        subscriptionExpiry.put(requestId, System.currentTimeMillis() + (long) monitorDuration * 60_000L);
    }

    public void notifyClients(Availability availability) {
        if (Objects.isNull(mailingList.get(availability.getFacilityId()))) return;
        for (UUID requestId : mailingList.get(availability.getFacilityId())) {
            if (System.currentTimeMillis() > subscriptionExpiry.get(requestId)) {   // Expired subscription
                removeSubscription(requestId);
                continue;
            }
            InetAddress clientAddress = mailingAddresses.get(requestId).getLeft();
            int clientPort = mailingAddresses.get(requestId).getRight();
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] data = Response.success(requestId,
                        Map.of(
                                "availability", availability.toBytes()
                        )
                ).toBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
                socket.send(packet);
            } catch (SerializationException sere) {
                log.error("[notifyClients] Failed to serialize notification", sere);
                throw new RuntimeException(sere);
            } catch (SocketException se) {
                log.error("[notifyClients] Failed to open socket", se);
                throw new RuntimeException(se);
            } catch (IOException ioe) {
                log.error("[notifyClients Failed to send notification", ioe);
                throw new RuntimeException(ioe);
            }
        }
    }

    public void removeSubscription(UUID requestId) {
        for (int facilityId : mailingList.keySet()) {
            mailingList.get(facilityId).remove(requestId);
        }
        mailingAddresses.remove(requestId);
        subscriptionExpiry.remove(requestId);
    }
}
