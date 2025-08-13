package io.github.yogur.prayerutils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Container for all five daily prayer times for a specific date and location.
 */
public class PrayerTimes {
    private final LocalDate date;
    private final Location location;
    private final Map<Prayer, PrayerTime> times;

    /**
     * Creates a prayer times object.
     * 
     * @param date     The date these prayer times are for
     * @param location The location these prayer times are calculated for
     * @param times    Map containing all five prayer times
     */
    public PrayerTimes(LocalDate date, Location location, Map<Prayer, PrayerTime> times) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (times == null) {
            throw new IllegalArgumentException("Times cannot be null");
        }

        // Validate that all five prayers are present
        for (Prayer prayer : Prayer.values()) {
            if (!times.containsKey(prayer)) {
                throw new IllegalArgumentException("Missing prayer time for " + prayer);
            }
        }

        this.date = date;
        this.location = location;
        this.times = Collections.unmodifiableMap(new EnumMap<>(times));
    }

    /**
     * Gets the date these prayer times are calculated for.
     * 
     * @return The date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the location these prayer times are calculated for.
     * 
     * @return The location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the Fajr (dawn) prayer time.
     * 
     * @return Fajr prayer time
     */
    public PrayerTime getFajr() {
        return times.get(Prayer.FAJR);
    }

    /**
     * Gets the sunrise time.
     * 
     * @return Sunrise time
     */
    public PrayerTime getSunrise() {
        return times.get(Prayer.SUNRISE);
    }

    /**
     * Gets the Dhuhr (noon) prayer time.
     * 
     * @return Dhuhr prayer time
     */
    public PrayerTime getDhuhr() {
        return times.get(Prayer.DHUHR);
    }

    /**
     * Gets the Asr (afternoon) prayer time.
     * 
     * @return Asr prayer time
     */
    public PrayerTime getAsr() {
        return times.get(Prayer.ASR);
    }

    /**
     * Gets the Maghrib (sunset) prayer time.
     * 
     * @return Maghrib prayer time
     */
    public PrayerTime getMaghrib() {
        return times.get(Prayer.MAGHRIB);
    }

    /**
     * Gets the Ishaa (night) prayer time.
     * 
     * @return Ishaa prayer time
     */
    public PrayerTime getIshaa() {
        return times.get(Prayer.ISHAA);
    }

    /**
     * Gets a specific prayer time.
     * 
     * @param prayer The prayer to get the time for
     * @return The prayer time
     */
    public PrayerTime getPrayerTime(Prayer prayer) {
        return times.get(prayer);
    }

    /**
     * Returns an unmodifiable map of all prayer times.
     * 
     * @return Map of prayer to prayer time
     */
    public Map<Prayer, PrayerTime> getAllTimes() {
        return times;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        PrayerTimes that = (PrayerTimes) obj;
        return date.equals(that.date) &&
                location.equals(that.location) &&
                times.equals(that.times);
    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + times.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("PrayerTimes{date=%s, location=%s, times=%s}", date, location, times);
    }
}
