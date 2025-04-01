package org.ketchup.bookie.server.handler;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public interface ExceptionHandler {
    Response handleException(Throwable throwable);
    Response handleException(Request request, Throwable throwable);
}
