package org.ketchup.bookie.server.service;

import lombok.AllArgsConstructor;
import org.ketchup.bookie.server.config.Config;
import org.ketchup.bookie.server.interceptor.RequestInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tomato.bookie.distributedSystem.message.Request;

import java.util.HashMap;

@Service
public class RequestListener implements InitializingBean {

    private final Config config;
    private final RequestInterceptor requestInterceptor;

    public RequestListener(Config config, @Qualifier("repoRefresh") RequestInterceptor requestInterceptor) {
        this.config = config;
        this.requestInterceptor = requestInterceptor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Init stuffs

        // Starts listener
        listenForRequests(config.getPort());
    }

    public void listenForRequests(int port) {
        // Listening on port
        // When receiving a request, deserialize it to Request object
        Request request = new Request(Request.Operation.BOOK_FACILITY, "hl", new HashMap<>());
        // Pass Request to interceptors
        request = requestInterceptor.intercept(request);
        // Pass Request object to handler
        try {
            handleClientRequest(request);
        } catch (Throwable throwable) {

        }
    }

    private void handleClientRequest(Request request) {
        switch (request.getOperation()) {
            case BOOK_FACILITY ->
        }
    }
}
