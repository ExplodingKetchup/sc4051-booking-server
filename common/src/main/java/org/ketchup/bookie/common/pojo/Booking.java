package org.ketchup.bookie.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ketchup.bookie.common.exception.SerializationException;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    public static final Booking NULL_INSTANCE = new Booking(-1, -1, -1, -1);

    private int bookingId;
    private int facilityId;
    private int bookingStartTime;   // Inclusive
    private int bookingEndTime;     // Exclusive

}
