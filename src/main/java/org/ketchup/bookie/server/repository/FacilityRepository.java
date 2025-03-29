package org.ketchup.bookie.server.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.server.util.CsvUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class FacilityRepository implements InitializingBean {

    private final BookingRepository bookingRepository;

    private final Map<Integer, Facility> facilities = new HashMap<>();

    public FacilityRepository(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Facility getFacilityById(int facilityId) {
        return facilities.get(facilityId);
    }

    public List<Facility> getFacilitiesByName(String name) {
        return facilities.values().stream().filter(facility -> StringUtils.equals(name, facility.getName())).toList();
    }

    public void loadFromCsv() throws IOException {
        CsvUtils.readFacilitiesFromCsv().forEach(facility -> facilities.put(facility.getId(), facility));
    }

    public void writeToCsv() throws IOException {
        CsvUtils.writeFacilitiesToCsv(new ArrayList<>(facilities.values()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadFromCsv();
        bookingRepository.addAllFacilities(facilities.values().stream().toList());
    }
}
