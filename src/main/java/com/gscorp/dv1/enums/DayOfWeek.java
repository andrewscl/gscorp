package com.gscorp.dv1.enums;

public enum DayOfWeek {
    MONDAY(1, "lunes"),
    TUESDAY(2, "martes"),
    WEDNESDAY(3, "miércoles"),
    THURSDAY(4, "jueves"),
    FRIDAY(5, "viernes"),
    SATURDAY(6, "sábado"),
    SUNDAY(7, "domingo");

    private final int dayNumber;
    private final String displayNameInSpanish;

    DayOfWeek(int dayNumber, String displayNameInSpanish) {
        this.dayNumber = dayNumber;
        this.displayNameInSpanish = displayNameInSpanish;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public String getDisplayNameInSpanish() {
        return displayNameInSpanish;
    }

    public String toLowerCaseDaysOfWeek() {
        return this.name().toLowerCase();
    }

    public static DayOfWeek fromDayOfWeek(int dayNumber) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getDayNumber() == dayNumber) {
                return day;
            }
        }
        throw new IllegalArgumentException("No enum constant for DayNumber: " + dayNumber);
    }
}
