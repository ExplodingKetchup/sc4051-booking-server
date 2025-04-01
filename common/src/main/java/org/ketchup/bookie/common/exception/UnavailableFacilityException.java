package org.ketchup.bookie.common.exception;

public class UnavailableFacilityException extends Exception {
    public UnavailableFacilityException() {
        super("The facility specified does not exist");
    }

    public UnavailableFacilityException(String message) {
        super(message);
    }

    public UnavailableFacilityException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnavailableFacilityException(Throwable cause) {
        super("The facility specified does not exist", cause);
    }
}
