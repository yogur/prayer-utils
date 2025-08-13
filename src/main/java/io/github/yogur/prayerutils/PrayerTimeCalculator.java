package io.github.yogur.prayerutils;

import java.time.LocalDate;

/**
 * Interface for calculating Islamic prayer times.
 * 
 * Implementations of this interface should calculate prayer times based on
 * astronomical
 * calculations, database lookups, or other methods as appropriate.
 */
public interface PrayerTimeCalculator {

    /**
     * Calculates all five daily prayer times for the given date, location, and
     * parameters.
     * 
     * @param date       The date to calculate prayer times for
     * @param location   The geographical location
     * @param parameters The calculation parameters (angles, methods, etc.)
     * @return Prayer times for all five daily prayers
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    PrayerTimes calculatePrayerTimes(LocalDate date, Location location, PrayerCalculationParameters parameters);

    /**
     * Calculates Fajr prayer time only.
     * 
     * @param date       The date to calculate for
     * @param location   The geographical location
     * @param parameters The calculation parameters
     * @return Fajr prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    PrayerTime calculateFajr(LocalDate date, Location location, PrayerCalculationParameters parameters);

    /**
     * Calculates Dhuhr prayer time only.
     * 
     * @param date       The date to calculate for
     * @param location   The geographical location
     * @param parameters The calculation parameters
     * @return Dhuhr prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    PrayerTime calculateDhuhr(LocalDate date, Location location, PrayerCalculationParameters parameters);

    /**
     * Calculates Asr prayer time only.
     * 
     * @param date       The date to calculate for
     * @param location   The geographical location
     * @param parameters The calculation parameters
     * @return Asr prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    PrayerTime calculateAsr(LocalDate date, Location location, PrayerCalculationParameters parameters);

    /**
     * Calculates Maghrib prayer time only.
     * 
     * @param date       The date to calculate for
     * @param location   The geographical location
     * @param parameters The calculation parameters
     * @return Maghrib prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    PrayerTime calculateMaghrib(LocalDate date, Location location, PrayerCalculationParameters parameters);

    /**
     * Calculates Ishaa prayer time only.
     * 
     * @param date       The date to calculate for
     * @param location   The geographical location
     * @param parameters The calculation parameters
     * @return Ishaa prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    PrayerTime calculateIshaa(LocalDate date, Location location, PrayerCalculationParameters parameters);
}