package org.ketchup.bookie.server.interceptor;

import lombok.AllArgsConstructor;
import org.ketchup.bookie.server.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tomato.bookie.distributedSystem.message.Request;

/**
 * Refresh data in repositories before handling any request
 */
@Component
@Qualifier("repoRefresh")
@AllArgsConstructor
public class RepositoryRefreshInterceptor implements RequestInterceptor {

    private final BookingRepository bookingRepository;

    @Override
    public Request intercept(Request request) {
        bookingRepository.refresh();
        return request;
    }
}
