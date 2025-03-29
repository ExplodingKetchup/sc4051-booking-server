package org.tomato.bookie.distributedSystem.operations;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;


public interface Operation {
    Response execute(Request request);
    boolean isIdempotent();
}