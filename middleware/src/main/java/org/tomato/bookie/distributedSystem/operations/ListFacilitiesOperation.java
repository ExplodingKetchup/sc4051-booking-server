package org.tomato.bookie.distributedSystem.operations;

import org.tomato.bookie.distributedSystem.message.Request;
import org.tomato.bookie.distributedSystem.message.Response;

import java.util.Map;

public class ListFacilitiesOperation implements Operation {
    public Response execute(Request request) {
        return Response.success(request.getRequestId(), 
            Map.of("facilities", facilityRepository.getAllNames()));
    }
    
    public boolean isIdempotent() { return true; }
}