package org.tomato.bookie.distributedSystem.monitoring;


import java.util.*;
import org.tomato.bookie.distributedSystem.message.Response;

public class MonitorManager {
    private final Map<String, List<MonitoringCallback>> monitors = new HashMap<>();
    
    public void register(String facilityName, MonitoringCallback callback) {
        monitors.computeIfAbsent(facilityName, k -> new ArrayList<>()).add(callback);
    }
    
    public void notifyAll(String facilityName, Response update) {
        monitors.getOrDefault(facilityName, Collections.emptyList())
               .forEach(c -> c.onUpdate(facilityName, update));
    }
}