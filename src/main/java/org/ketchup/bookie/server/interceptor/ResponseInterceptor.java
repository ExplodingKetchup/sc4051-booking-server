package org.ketchup.bookie.server.interceptor;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public interface ResponseInterceptor {
    Response intercept(Request request, Response response);
}
