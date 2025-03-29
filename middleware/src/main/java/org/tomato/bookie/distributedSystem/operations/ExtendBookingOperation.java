package org.tomato.bookie.distributedSystem.operations;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;
import java.util.Map;

public class ExtendBookingOperation implements Operation {
    public Response execute(Request request) {
        Booking booking = bookingManager.extendBooking(
            (String)request.getParameters().get("confirmationId"),
            (int)request.getParameters().get("minutes")
        );
        return Response.success(request.getRequestId(),
            Map.of("newEndTime", booking.getEndTime()));
    }
    
    public boolean isIdempotent() { return false; }
}