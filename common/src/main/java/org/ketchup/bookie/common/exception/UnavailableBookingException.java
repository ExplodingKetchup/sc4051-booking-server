package org.ketchup.bookie.common.exception;

public class UnavailableBookingException extends Exception {
    public UnavailableBookingException() {
        super("Booking ID is not recorded, or the booking operation has failed due to booking conflicts");
    }

    public UnavailableBookingException(String message) {
        super(message);
    }

    public UnavailableBookingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnavailableBookingException(Throwable cause) {
        super("Booking ID is not recorded, or the booking operation has failed due to booking conflicts", cause);
    }
}
