package org.ketchup.bookie.server.service;

import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.UnavailableBookingException;
import org.ketchup.bookie.common.exception.UnavailableFacilityException;
import org.ketchup.bookie.common.pojo.Booking;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.server.config.Constants;
import org.ketchup.bookie.server.repository.BookingRepository;
import org.ketchup.bookie.server.repository.FacilityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class BookingManager {

    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;
    private final Random random = new Random(System.currentTimeMillis());


    public BookingManager(BookingRepository bookingRepository, FacilityRepository facilityRepository) {
        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
    }

    public Booking queryBooking(int bookingId) throws UnavailableBookingException {
        Booking booking = bookingRepository.queryBooking(bookingId);
        if (booking == Booking.NULL_INSTANCE) {
            throw new UnavailableBookingException();
        }
        return booking;
    }

    public void checkAvailability(int facilityId, int checkTimeStart, int checkTimeEnd) throws UnavailableFacilityException {
        if (facilityRepository.getFacilityById(facilityId).equals(Facility.NULL_INSTANCE)) {
            throw new UnavailableFacilityException();
        }
        if (!bookingRepository.checkAvailability(facilityId, checkTimeStart, checkTimeEnd)) {
            throw new UnavailableFacilityException("Facility is not available during the specified time");
        }
    }

    public void changeBooking(int bookingId, int offsetTime) throws UnavailableBookingException, UnavailableFacilityException {
        if (!bookingRepository.changeBooking(bookingId, offsetTime)) {
            throw new UnavailableBookingException();
        }
    }

    public void extendBooking(int bookingId, int offsetTime) throws UnavailableBookingException, UnavailableFacilityException {
        if (!bookingRepository.extendBooking(bookingId, offsetTime)) {
            throw new UnavailableBookingException();
        }
    }

    public int addBooking(int facilityId, int startTime, int endTime) throws UnavailableFacilityException, UnavailableBookingException {
        // Generate booking Id
        int bookingId = random.nextInt(1, Integer.MAX_VALUE);
        while (bookingRepository.isBookingIdExist(bookingId)) {
            bookingId = random.nextInt(1, Integer.MAX_VALUE);
        }
        Booking booking = new Booking(bookingId, facilityId, startTime, endTime);
        // Verify booking facility
        if (facilityRepository.getFacilityById(booking.getFacilityId()).equals(Facility.NULL_INSTANCE)) {
            log.error("[addBooking] Facility ID not found");
            throw new UnavailableFacilityException("[addBooking] Facility ID not found");
        }
        // Add to booking repository
        if (!bookingRepository.addBooking(booking)) {
            log.error("[addBooking] Booking conflict");
            throw new UnavailableBookingException("[addBooking] Booking conflict");
        }
        return bookingId;
    }
}
