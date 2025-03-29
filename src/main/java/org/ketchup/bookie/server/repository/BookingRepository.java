package org.ketchup.bookie.server.repository;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ketchup.bookie.common.pojo.Booking;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.server.config.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Note: All timestamps provided will be round up to match the starting point of slots.
 */
@Repository
@Slf4j
@NoArgsConstructor
public class BookingRepository implements InitializingBean {

    private long currentTimestamp;
    private long firstSlotIdx;
    private long lastSlotIdx;

    // facilityId : { slotIdx : bookingId }
    private final Map<Integer, Map<Long, Integer>> bookingTimeslots = new ConcurrentHashMap<>();

    private final Map<Integer, Booking> bookingMap = new ConcurrentHashMap<>();

    public boolean isBookingIdExist(int bookingIdToCheck) {
        return bookingMap.containsKey(bookingIdToCheck);
    }

    public boolean addBooking(Booking booking) {
        if (getSlotIdx(booking.getBookingTimeStart()) < firstSlotIdx) {
            throw new IllegalArgumentException("[addBooking] Booking is made for time slots in the past");
        }
        if (checkFacilityAvailability(booking.getFacilityId(), booking.getBookingTimeStart(), booking.getBookingTimeSlots())) {
            long slotIdxOffset = getSlotIdx(booking.getBookingTimeStart());
            for (int slotIdx = 0; slotIdx < booking.getBookingTimeSlots(); slotIdx++) {
                bookingTimeslots.get(booking.getFacilityId()).put(slotIdx + slotIdxOffset, booking.getBookingId());
            }
            bookingMap.put(booking.getBookingId(), booking);
            return true;
        }
        return false;
    }

    public boolean checkFacilityAvailability(int facilityId, long startingTimestamp, int nSlots) {
        if (nSlots <= 0) {
            log.error("[checkFacilityAvailability] Number of slot must be at least 1");
            throw new IllegalArgumentException("[checkFacilityAvailability] Number of slot must be at least 1");
        }
        long slotIdxOffset = getSlotIdx(startingTimestamp);
        for (int slotIdx = 0; slotIdx < nSlots; slotIdx++) {
            if (getBookingIdForFacilityAtSlot(facilityId, slotIdx + slotIdxOffset) < 0) {
                return false;
            }
        }
        return true;
    }

    public int getBookingIdForFacilityAtSlot(int facilityId, long slotIdx) {
        try {
            return Optional.ofNullable(bookingTimeslots.get(facilityId).get(slotIdx)).orElse(-1);
        } catch (NullPointerException npe) {
            log.error("[getBookingIdForFacilityAtSlot] Facility Id may not exist");
            throw new IllegalArgumentException("[getBookingIdForFacilityAtSlot] Facility Id may not exist");
        }
    }

    public void addFacility(Facility facility) {
        bookingTimeslots.put(facility.getId(), new ConcurrentHashMap<>((int) (lastSlotIdx - firstSlotIdx + 1)));
        refresh();
    }

    public void addAllFacilities(List<Facility> facilityList) {
        for (Facility facility : facilityList) {
            if (!bookingTimeslots.containsKey(facility.getId())) {
                bookingTimeslots.put(facility.getId(), new ConcurrentHashMap<>((int) (lastSlotIdx - firstSlotIdx + 1)));
            }
        }
        refresh();
    }

    public void dropFacility(Facility facility) {
        bookingMap.remove(facility.getId());
    }

    public void refresh() {
        currentTimestamp = System.currentTimeMillis();
        calculateFirstSlotIdx();
        calculateLastSlotIdx();
        // Throw away old records (bookingTimeslots)
        for (Integer facilityId : bookingTimeslots.keySet()) {
            Map<Long, Integer> facilityRecord = bookingTimeslots.get(facilityId);
            facilityRecord.forEach((slotIdx, bookingId) -> {
                if (slotIdx < firstSlotIdx) {
                    facilityRecord.remove(slotIdx);
                }
            });
        }
        // Add new slots (bookingTimeslots)
        for (long slotIdx = firstSlotIdx; slotIdx <= lastSlotIdx; slotIdx++) {
            for (Integer facilityId : bookingTimeslots.keySet()) {
                bookingTimeslots.get(facilityId).putIfAbsent(slotIdx, -1);
            }
        }
        // Remove expired bookings
        for (Integer bookingId : bookingMap.keySet()) {
            Booking booking = bookingMap.get(bookingId);
            if (getSlotIdx(booking.getBookingTimeStart()) + booking.getBookingTimeSlots() - 1 < firstSlotIdx) {
                bookingMap.remove(bookingId);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private long getSlotIdx(long slotStartingTimestamp) {
        return (slotStartingTimestamp / Constants.BOOKING_SLOT_LENGTH_MILLIS) + (slotStartingTimestamp % Constants.BOOKING_SLOT_LENGTH_MILLIS > 0 ? 1 : 0);
    }

    private void calculateFirstSlotIdx() {
        firstSlotIdx = getSlotIdx(currentTimestamp);
    }

    private void calculateLastSlotIdx() {
        LocalDate currentDate = LocalDate.now();
        LocalDate targetDate = currentDate.plusDays(8);
        LocalDateTime targetDateTime = targetDate.atStartOfDay();
        long unixTimestamp = targetDateTime.toEpochSecond(ZoneOffset.UTC) * 1000L;
        lastSlotIdx = getSlotIdx(unixTimestamp) - 1;
    }

    private long normalizeSlotTimestamp(long slotStartingTimestamp) {
        return getSlotIdx(slotStartingTimestamp) * Constants.BOOKING_SLOT_LENGTH_MILLIS;
    }
}
