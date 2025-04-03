package org.ketchup.bookie.server.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.SerializationException;
import org.ketchup.bookie.common.pojo.Availability;
import org.ketchup.bookie.common.util.SerializeUtils;
import org.ketchup.bookie.server.repository.BookingRepository;
import org.ketchup.bookie.server.service.AvailabilityMonitoringService;
import org.springframework.stereotype.Service;
import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

import java.util.ArrayList;

@Service
@Slf4j
public class ResponseInterceptorImpl implements ResponseInterceptor {

    private final BookingRepository bookingRepository;
    private final AvailabilityMonitoringService availabilityMonitoringService;

    public ResponseInterceptorImpl(BookingRepository bookingRepository, AvailabilityMonitoringService availabilityMonitoringService) {
        this.bookingRepository = bookingRepository;
        this.availabilityMonitoringService = availabilityMonitoringService;
    }

    @Override
    public Response intercept(Request request, Response response) {
        int facilityId = -1;
        if (request.getOperation().equals(Request.Operation.BOOK_FACILITY)) {
            try {
                facilityId = SerializeUtils.deserializeInt(request.getParameters().get("facilityId"));
            } catch (SerializationException se) {
                log.error("[intercept] Unable to obtain facilityId, no notification will be sent");
            }
        }
        if (request.getOperation().equals(Request.Operation.CHANGE_BOOKING) || request.getOperation().equals(Request.Operation.EXTEND_BOOKING)) {
            try {
                facilityId = bookingRepository.queryBooking(
                        SerializeUtils.deserializeInt(request.getParameters().get("bookingId"))).getFacilityId();
            } catch (SerializationException e) {
                log.error("[intercept] Unable to obtain facilityId, no notification will be sent");
            }
        }
        if (facilityId >= 0) {
            Availability availability = new Availability();
            availability.setFacilityId(facilityId);
            availability.setBooked(bookingRepository.exportAvailability(facilityId));
            availabilityMonitoringService.notifyClients(availability);
        }
        return response;
    }
}
