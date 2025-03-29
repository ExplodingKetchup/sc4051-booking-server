package org.ketchup.bookie.server.service;

import org.ketchup.bookie.common.pojo.Booking;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.server.repository.BookingRepository;
import org.ketchup.bookie.server.repository.FacilityRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BookingManager {

    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;
    private final Random random = new Random(System.currentTimeMillis());


    public BookingManager(BookingRepository bookingRepository, FacilityRepository facilityRepository) {
        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
    }

    public Booking queryBooking(int bookingId) {

    }

    /**
     * Generate new bookingId then add to repository
     * @param booking
     * @return
     */
    public int addBooking(Booking booking) {
        int bookingId = random.nextInt(1, Integer.MAX_VALUE);
        while (bookingRepository.isBookingIdExist(bookingId)) {
            bookingId = random.nextInt(1, Integer.MAX_VALUE);
        }
        // Verify booking facility
        if (facilityRepository.getFacilityById(booking.getFacilityId()).equals(Facility.NULL_INSTANCE)) {
            throw new IllegalArgumentException("[addBooking] Facility ID not found");
        }
        // Add to booking repository
        if (bookingRepository.addBooking(booking)) {

        }
    }
}
