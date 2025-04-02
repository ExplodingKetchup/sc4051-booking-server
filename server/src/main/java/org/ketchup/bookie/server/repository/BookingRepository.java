package org.ketchup.bookie.server.repository;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.exception.InternalServerError;
import org.ketchup.bookie.common.exception.UnavailableFacilityException;
import org.ketchup.bookie.common.pojo.Booking;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.server.config.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Note: All timestamps provided will be round up to match the starting point of slots.
 */
@Repository
@Slf4j
@NoArgsConstructor
public class BookingRepository implements InitializingBean {

    // facilityId : { time : bookingId }
    private final Map<Integer, List<Integer>> bookingTimeslots = new ConcurrentHashMap<>();

    private final Map<Integer, Booking> bookingMap = new ConcurrentHashMap<>();

    public boolean isBookingIdExist(int bookingIdToCheck) {
        return bookingMap.containsKey(bookingIdToCheck);
    }

    public Booking queryBooking(int bookingId) {
        return Optional.ofNullable(bookingMap.get(bookingId)).orElse(Booking.NULL_INSTANCE);
    }

    public boolean addBooking(Booking booking) throws UnavailableFacilityException {
        if (booking.getBookingStartTime() < 0 ||
                booking.getBookingEndTime() <= booking.getBookingStartTime() ||
                booking.getBookingEndTime() > Constants.MINUTES_IN_WEEK
        ) {
            throw new IllegalArgumentException("[addBooking] Invalid booking time");
        }
        if (bookingMap.containsKey(booking.getBookingId())) {
            throw new IllegalArgumentException("[addBooking] Booking with the same ID already exists");
        }
        if (checkAvailability(booking.getFacilityId(), booking.getBookingStartTime(), booking.getBookingEndTime())) {
            for (int time = booking.getBookingStartTime(); time < booking.getBookingEndTime(); time++) {
                bookingTimeslots.get(booking.getFacilityId()).set(time, booking.getBookingId());
            }
            bookingMap.put(booking.getBookingId(), booking);
            return true;
        }
        return false;
    }

    public boolean removeBooking(int bookingId) {
        if (!bookingMap.containsKey(bookingId)) return false;
        Booking booking = bookingMap.get(bookingId);
        for (int time = booking.getBookingStartTime(); time < booking.getBookingEndTime(); time++) {
            int currentBookingIdAtSlot = bookingTimeslots.get(booking.getFacilityId()).get(time);
            if (currentBookingIdAtSlot != bookingId) {
                InternalServerError ise = new InternalServerError("Inconsistency between bookingTimeslots and bookingMap found: " +
                        "Time [" + time + "] belongs to both booking [" + bookingId + "] (bookingMap) and booking " +
                        "[" + currentBookingIdAtSlot + "] (bookingTimeslots)");
                log.error("[removeBooking]", ise);
                throw ise;
            }
            bookingTimeslots.get(booking.getFacilityId()).set(time, -1);
        }
        bookingMap.remove(bookingId);
        return true;
    }

    public boolean changeBooking(int bookingId, int offset) throws UnavailableFacilityException {
        Booking originalBooking = bookingMap.get(bookingId);
        if (!removeBooking(bookingId)) return false;
        return addBooking(new Booking(
                originalBooking.getBookingId(),
                originalBooking.getFacilityId(),
                originalBooking.getBookingStartTime() + offset,
                originalBooking.getBookingEndTime() + offset
        ));
    }

    public boolean extendBooking(int bookingId, int offset) throws UnavailableFacilityException {
        Booking originalBooking = bookingMap.get(bookingId);
        if (!removeBooking(bookingId)) return false;
        return addBooking(new Booking(
                originalBooking.getBookingId(),
                originalBooking.getFacilityId(),
                originalBooking.getBookingStartTime(),
                originalBooking.getBookingEndTime() + offset
        ));
    }

    /**
     *
     * @param facilityId
     * @param startTime Inclusive
     * @param endTime   Exclusive
     * @return
     */
    public boolean checkAvailability(int facilityId, int startTime, int endTime) throws UnavailableFacilityException {
        List<Integer> facilityAvailability = Optional.ofNullable(bookingTimeslots.get(facilityId)).orElseThrow(UnavailableFacilityException::new);
        for (int time = startTime; time < endTime; time++) {
            if (facilityAvailability.get(time) > 0) return false;
        }
        return true;
    }

    public List<Boolean> exportAvailability(int facilityId) {
        return bookingTimeslots.get(facilityId).stream().map(bookingId -> bookingId >= 0).toList();
    }

    public void addFacility(Facility facility) {
        List<Integer> timeSlotsForFacility = new ArrayList<>(Constants.MINUTES_IN_WEEK);
        for (int i = 0; i <= Constants.MINUTES_IN_WEEK; i++) {
            timeSlotsForFacility.add(-1);
        }
        bookingTimeslots.putIfAbsent(facility.getId(), timeSlotsForFacility);
    }

    public void addAllFacilities(List<Facility> facilityList) {
        for (Facility facility : facilityList) {
            addFacility(facility);
        }
    }

    public void dropFacility(Facility facility) {
        bookingMap.remove(facility.getId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
