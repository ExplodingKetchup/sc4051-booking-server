package org.ketchup.bookie.server.config;

import java.io.File;

public class Constants {
    public static final String FACILITIES_CSV_PATH = "data" + File.separator + "facilities.csv";
    public static final long BOOKING_SLOT_LENGTH_MILLIS = 30 * 60 * 1000;
    public static final long BOOKING_TIME_WINDOW_DAYS = 7L;
}
