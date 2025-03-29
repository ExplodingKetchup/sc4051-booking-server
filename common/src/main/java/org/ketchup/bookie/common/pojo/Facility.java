package org.ketchup.bookie.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ketchup.bookie.common.enums.FacilityType;

@Data
@AllArgsConstructor
public class Facility {
    private final int id;
    private String name;
    private FacilityType type;
}
