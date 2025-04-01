package org.ketchup.bookie.common.exception;

public class MalformedResponseException extends Exception {
    public MalformedResponseException() {
    }

    public MalformedResponseException(String message) {
        super(message);
    }

    public MalformedResponseException(Throwable cause) {
        super(cause);
    }

    public MalformedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
