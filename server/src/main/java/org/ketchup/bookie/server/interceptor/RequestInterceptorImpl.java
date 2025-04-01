package org.ketchup.bookie.server.interceptor;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tomato.bookie.distributedSystem.message.Request;

/**
 * Refresh data in repositories before handling any request
 */
@Component
@AllArgsConstructor
public class RequestInterceptorImpl implements RequestInterceptor {

    @Override
    public Request intercept(Request request) {
        return request;
    }
}
