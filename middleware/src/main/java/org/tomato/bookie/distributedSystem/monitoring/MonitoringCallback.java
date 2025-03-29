package org.tomato.bookie.distributedSystem.monitoring;

import org.tomato.bookie.distributedSystem.message.Response;

public interface MonitoringCallback {
    void onUpdate(String facilityName, Response update);
}
