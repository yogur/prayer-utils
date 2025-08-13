package io.github.yogur.prayerutils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * POJOs used for data-driven tests loaded from JSON.
 */
class DataFixtures {

    static class CaseFile {
        public String description;
        public Case[] cases;
    }

    static class Case {
        public Input input;
        public Expected expected;
    }

    static class Input {
        public String date; // ISO yyyy-MM-dd
        public double latitude;
        public double longitude;
        public double elevation;
        public String timezone; // IANA tz
        public double fajrAngle;
        public double ishaaAngle;
        public String asrMethod; // SHAFII or HANAFI
        public boolean useAqrabAlBilad;
    }

    static class Expected {
        public String fajr;    // HH:mm
        public String sunrise; // HH:mm
        public String dhuhr;   // HH:mm
        public String asr;     // HH:mm
        public String maghrib; // HH:mm
        public String ishaa;   // HH:mm
    }

    static LocalDate parseDate(String s) { return LocalDate.parse(s); }

    static LocalTime parseTime(String s) { return LocalTime.parse(s); }

    static ZoneId parseZone(String s) { return ZoneId.of(s); }
}


