package org.tomato.bookie.distributedSystem.semantics;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

public interface InvocationSemantic {
    Response execute(Request request);
}