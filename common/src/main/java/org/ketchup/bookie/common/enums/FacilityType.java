package org.ketchup.bookie.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FacilityType implements SerializableEnum {
    MEETING_ROOM(0),
    STANDARD_CLASSROOM(1),
    LECTURE_THEATER(2),
    STUDY_POD(3),
    FOOTBALL_COURT(4),
    BADMINTON_COURT(5),

    UNKNOWN(-1);


    private final int value;

    public static FacilityType fromValue(Integer value) {
        return switch (value) {
            case 0 -> MEETING_ROOM;
            case 1 -> STANDARD_CLASSROOM;
            case 2 -> LECTURE_THEATER;
            case 3 -> STUDY_POD;
            case 4 -> FOOTBALL_COURT;
            case 5 -> BADMINTON_COURT;
            default -> UNKNOWN;
        };
    }
}
