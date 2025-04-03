package org.ketchup.bookie.server.interceptor;

import org.tomato.bookie.distributedSystem.message.Request;

public interface RequestInterceptor {
    /**
     * Intercept every deserialized request to perform tasks before it is handled by business logic services.
     * @param request
     * @return
     */
    Request intercept(Request request);
}
