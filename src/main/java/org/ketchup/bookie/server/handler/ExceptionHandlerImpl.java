package org.ketchup.bookie.server.handler;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public class ExceptionHandlerImpl implements ExceptionHandler{
    @Override
    public Response handleException(Request request, Throwable throwable) {
        return null;
    }
}
