package org.tomato.bookie.distributedSystem.faultolerance;

import java.util.*;
import java.util.concurrent.*;

public class Deduplicator {
    private final ConcurrentMap<UUID, Long> cache = new ConcurrentHashMap<>();
    private static final long TTL_MS = 300_000; // 5 minutes

    public boolean isDuplicate(UUID requestId) {
        return cache.containsKey(requestId);
    }

    public void recordRequest(UUID requestId) {
        cache.put(requestId, System.currentTimeMillis());
        scheduleCleanup(requestId);
    }

    private void scheduleCleanup(UUID requestId) {
        new Timer().schedule(new TimerTask() {
            public void run() { cache.remove(requestId); }
        }, TTL_MS);
    }
}