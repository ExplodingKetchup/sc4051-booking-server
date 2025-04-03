package org.ketchup.bookie.server.interceptor;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public interface ResponseInterceptor {
    /**
     * Intercept every successful responses, perform some tasks before return the response to {@link org.ketchup.bookie.server.service.RequestListener} for serialization and sending to client.
     * @param request
     * @param response
     * @return
     */
    Response intercept(Request request, Response response);
}
