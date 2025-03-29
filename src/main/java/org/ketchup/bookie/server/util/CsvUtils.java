package org.ketchup.bookie.server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.ketchup.bookie.common.enums.FacilityType;
import org.ketchup.bookie.common.pojo.Facility;
import org.ketchup.bookie.server.config.Constants;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class CsvUtils {

    public static final List<String> FACILITY_CSV_COLS = List.of("id", "name", "type");

    public static List<Facility> readFacilitiesFromCsv() throws IOException {
        List<Facility> facilities = new ArrayList<>();
        try (CSVParser csvParser = CSVFormat.DEFAULT.parse(
                new BufferedReader(new FileReader(Constants.FACILITIES_CSV_PATH, StandardCharsets.UTF_8)))
        ) {

            for (CSVRecord csvRecord : csvParser) {
                int id = Integer.parseInt(csvRecord.get(FACILITY_CSV_COLS.get(0)));
                String name = csvRecord.get(FACILITY_CSV_COLS.get(1));
                int typeValue = Integer.parseInt(csvRecord.get(FACILITY_CSV_COLS.get(2)));
                facilities.add(new Facility(id, name, FacilityType.fromValue(typeValue)));
            }

        } catch (IOException ioe) {
            log.error("[readFacilitiesFromCsv] Error reading CSV file", ioe);
            throw ioe;
        }
        return facilities;
    }

    public static void writeFacilitiesToCsv(List<Facility> facilities) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(FACILITY_CSV_COLS.toArray(String[]::new)).build();
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.FACILITIES_CSV_PATH, StandardCharsets.UTF_8));
                CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)
        ) {
            for (Facility facility : facilities) {
                csvPrinter.printRecord(facility.getId(), facility.getName(), facility.getType().getValue());
            }
            csvPrinter.flush();
        } catch (IOException ioe) {
            log.error("[writeFacilitiesToCsv] Error writing to csv ", ioe);
            throw ioe;
        }
    }
}
