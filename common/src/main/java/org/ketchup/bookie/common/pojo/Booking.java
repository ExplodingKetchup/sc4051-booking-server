package org.ketchup.bookie.common.pojo;

import lombok.Data;

@Data
public class Booking {
    private int bookingId;
    private int userId;
    private int facilityId;
    private long bookingTimeStart;
    private int bookingTimeSlots;   // 1 slot = 30 mins
}
