package io.github.yogur.prayerutils;

/**
 * Enumeration of the five daily Islamic prayers and sunrise.
 */
public enum Prayer {
    /** Dawn prayer, performed before sunrise */
    FAJR,
    /** Time of sunrise (not a prayer, but often calculated as it signifies the end of Fajr prayer time) */
    SUNRISE,
    /** Noon prayer, performed when the sun starts moving away from its zenith position */
    DHUHR,
    /** Afternoon prayer, performed in the afternoon */
    ASR,
    /** Sunset prayer, performed just after sunset */
    MAGHRIB,
    /** Night prayer, performed when twilight disappears */
    ISHAA
}
