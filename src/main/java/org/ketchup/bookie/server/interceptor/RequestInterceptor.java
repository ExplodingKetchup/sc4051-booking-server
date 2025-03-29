package org.ketchup.bookie.server.interceptor;

import org.tomato.bookie.distributedSystem.message.Request;

public interface RequestInterceptor {
    Request intercept(Request request);
}
