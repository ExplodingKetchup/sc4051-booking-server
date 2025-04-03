package org.ketchup.bookie.server.handler;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public interface ExceptionHandler {
    /**
     * Wraps the thrown exception in a {@link Response}. This variation is for errors which occurs before the deserialized request is available.
     * @param throwable
     * @return
     */
    Response handleException(Throwable throwable);

    /**
     * Wraps the thrown exception in a {@link Response}. This variation is for errors which occurs after the deserialized request is available.
     * @param request
     * @param throwable
     * @return
     */
    Response handleException(Request request, Throwable throwable);
}
