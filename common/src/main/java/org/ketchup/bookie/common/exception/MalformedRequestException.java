package org.ketchup.bookie.common.exception;

public class MalformedRequestException extends Exception {
    public MalformedRequestException() {
    }

    public MalformedRequestException(String message) {
        super(message);
    }

    public MalformedRequestException(Throwable cause) {
        super(cause);
    }

    public MalformedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
