package org.ketchup.bookie.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.SerializationException;
import org.springframework.stereotype.Component;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class ExceptionHandlerImpl implements ExceptionHandler{
    @Override
    public Response handleException(Throwable throwable) {
        try {
            return Response.applicationError(UUID.randomUUID(), formatExceptionAsString(throwable));
        } catch (SerializationException se) {
            log.error("[handleException] Got issues while creating application error response", se);
            return Response.applicationError(UUID.randomUUID());
        }
    }

    @Override
    public Response handleException(Request request, Throwable throwable) {
        try {
            return Response.applicationError(request.getRequestId(), formatExceptionAsString(throwable));
        } catch (SerializationException se) {
            log.error("[handleException] Got issues while creating application error response", se);
            return Response.applicationError(request.getRequestId());
        }
    }

    private String formatExceptionAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder(throwable.getClass().getName());
        sb.append(" - ");
        String methodNamePrefix = throwable.getMessage().split(" ")[0];
        if (methodNamePrefix.startsWith("[") && methodNamePrefix.endsWith("]")) {
            sb.append(throwable.getMessage().substring(methodNamePrefix.length()).trim());
        }
        if (Objects.nonNull(throwable.getCause())) {
            sb.append(" - Caused by: ").append(throwable.getCause().getClass().getName());
        }
        return sb.toString();
    }
}
