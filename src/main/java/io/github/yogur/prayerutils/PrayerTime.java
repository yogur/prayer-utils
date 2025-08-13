package io.github.yogur.prayerutils;

import java.time.LocalTime;

/**
 * Represents a calculated prayer time.
 */
public class PrayerTime {
    private final Prayer prayer;
    private final LocalTime time;
    private final boolean isAqrabAlBiladCalculation;
    private final String calculationMethod;

    /**
     * Creates a prayer time without calculation method metadata.
     * 
     * @param prayer                    The prayer this time is for
     * @param time                      The calculated time
     * @param isAqrabAlBiladCalculation Whether this time was calculated using Aqrab
     *                                  al-Bilad method
     */
    public PrayerTime(Prayer prayer, LocalTime time, boolean isAqrabAlBiladCalculation) {
        this(prayer, time, isAqrabAlBiladCalculation, null);
    }

    /**
     * Creates a prayer time with calculation method metadata.
     * 
     * @param prayer                    The prayer this time is for
     * @param time                      The calculated time
     * @param isAqrabAlBiladCalculation Whether this time was calculated using Aqrab
     *                                  al-Bilad method
     * @param calculationMethod         Optional description of the calculation
     *                                  method used
     */
    public PrayerTime(Prayer prayer, LocalTime time, boolean isAqrabAlBiladCalculation, String calculationMethod) {
        if (prayer == null) {
            throw new IllegalArgumentException("Prayer cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }

        this.prayer = prayer;
        this.time = time;
        this.isAqrabAlBiladCalculation = isAqrabAlBiladCalculation;
        this.calculationMethod = calculationMethod;
    }

    /**
     * Gets the prayer this time represents.
     * 
     * @return The prayer type
     */
    public Prayer getPrayer() {
        return prayer;
    }

    /**
     * Gets the calculated prayer time.
     * 
     * @return The time of this prayer
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Indicates whether this time was calculated using the Aqrab al-Bilad method.
     * This method is used for extreme latitudes where normal calculations are not possible.
     * 
     * @return true if calculated using Aqrab al-Bilad method, false otherwise
     */
    public boolean isAqrabAlBiladCalculation() {
        return isAqrabAlBiladCalculation;
    }

    /**
     * Gets a description of the calculation method used for this prayer time.
     * 
     * @return Description of the calculation method, or null if not specified
     */
    public String getCalculationMethod() {
        return calculationMethod;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        PrayerTime that = (PrayerTime) obj;
        return isAqrabAlBiladCalculation == that.isAqrabAlBiladCalculation &&
                prayer == that.prayer &&
                time.equals(that.time) &&
                (calculationMethod != null ? calculationMethod.equals(that.calculationMethod)
                        : that.calculationMethod == null);
    }

    @Override
    public int hashCode() {
        int result = prayer.hashCode();
        result = 31 * result + time.hashCode();
        result = 31 * result + (isAqrabAlBiladCalculation ? 1 : 0);
        result = 31 * result + (calculationMethod != null ? calculationMethod.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("PrayerTime{%s at %s%s%s}",
                prayer,
                time,
                isAqrabAlBiladCalculation ? " (Aqrab al-Bilad)" : "",
                calculationMethod != null ? " [" + calculationMethod + "]" : "");
    }
}