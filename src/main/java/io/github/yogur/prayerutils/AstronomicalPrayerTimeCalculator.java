package io.github.yogur.prayerutils;

import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;
import net.e175.klaus.solarpositioning.SolarPosition;
import net.e175.klaus.solarpositioning.SunriseResult;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Astronomical implementation of prayer time calculator using the NREL SPA
 * algorithm.
 * 
 * <p>
 * This implementation uses accurate astronomical calculations based on the
 * Solar Position
 * Algorithm (SPA) developed by the National Renewable Energy Laboratory (NREL)
 * for
 * high-precision prayer time calculations.
 * </p>
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>High accuracy solar position calculations</li>
 * <li>Support for extreme latitudes using Aqrab al-Bilad method</li>
 * <li>Accounts for atmospheric conditions (pressure, temperature)</li>
 * <li>Multiple Asr calculation methods (Shafi'i/Hanafi)</li>
 * </ul>
 */
public class AstronomicalPrayerTimeCalculator implements PrayerTimeCalculator {

    /**
     * Calculates all five daily prayer times using astronomical methods.
     * 
     * <p>
     * This implementation uses the NREL SPA algorithm for high-precision
     * calculations.
     * For extreme latitudes where normal calculations fail, the Aqrab al-Bilad
     * method
     * may be used if enabled in parameters.
     * </p>
     * 
     * @param date       the date to calculate prayer times for
     * @param location   the geographical location
     * @param parameters calculation parameters including angles and methods
     * @return complete prayer times for all five daily prayers
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException    if calculations fail for extreme latitudes
     *                                  without Aqrab al-Bilad
     */
    @Override
    public PrayerTimes calculatePrayerTimes(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        validateInputs(date, location, parameters);

        // Calculate DeltaT once and reuse for all prayer calculations
        double deltaT = DeltaT.estimate(date);

        Map<Prayer, PrayerTime> times = new EnumMap<>(Prayer.class);
        times.put(Prayer.FAJR, calculateFajr(date, location, parameters, deltaT));
        times.put(Prayer.SUNRISE, calculateSunrise(date, location, parameters, deltaT));
        times.put(Prayer.DHUHR, calculateDhuhr(date, location, parameters, deltaT));
        times.put(Prayer.ASR, calculateAsr(date, location, parameters, deltaT));
        times.put(Prayer.MAGHRIB, calculateMaghrib(date, location, parameters, deltaT));
        times.put(Prayer.ISHAA, calculateIshaa(date, location, parameters, deltaT));

        return new PrayerTimes(date, location, times);
    }

    /**
     * Calculates Fajr prayer time using astronomical methods.
     * 
     * <p>
     * Uses the configured Fajr angle below the horizon. For extreme latitudes,
     * may fall back to Aqrab al-Bilad method if enabled.
     * </p>
     * 
     * @param date       the date to calculate for
     * @param location   the geographical location
     * @param parameters calculation parameters
     * @return Fajr prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException    if the sun does not reach the required
     *                                  elevation due to polar day/night and Aqrab
     *                                  al-Bilad is disabled
     */
    @Override
    public PrayerTime calculateFajr(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        return calculateFajr(date, location, parameters, DeltaT.estimate(date));
    }

    /**
     * Private method that reuses deltaT to avoid recalculation.
     */
    private PrayerTime calculateFajr(LocalDate date, Location location, PrayerCalculationParameters parameters,
            double deltaT) {
        validateInputs(date, location, parameters);

        // Use negative angle for sunrise calculation (sun below horizon)
        double elevationAngle = -parameters.getFajrAngle();

        ZonedDateTime day = date.atStartOfDay(location.getTimezone());
        SunriseResult result = computeSunriseTransitSet(day, location, deltaT, elevationAngle, "Fajr");

        if (result instanceof SunriseResult.RegularDay regularDay) {
            LocalTime fajrTime = regularDay.sunrise().toLocalTime();
            return new PrayerTime(Prayer.FAJR, fajrTime, false,
                    String.format("SPA astronomical calculation (%.1f°)", parameters.getFajrAngle()));
        }
        // Non-regular day
        if (parameters.useAqrabAlBilad()) {
            return calculateFajrAqrabAlBilad(date, location, parameters, deltaT);
        }
        throw nonRegularException(date, Prayer.FAJR, result);
    }

    /**
     * Calculates sunrise time (end of Fajr window).
     * 
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException    if the day is polar day (sun above horizon
     *                                  all day)
     *                                  or polar night (sun below horizon all day)
     */
    private PrayerTime calculateSunrise(LocalDate date, Location location, PrayerCalculationParameters parameters,
            double deltaT) {
        validateInputs(date, location, parameters);

        ZonedDateTime day = date.atStartOfDay(location.getTimezone());
        SunriseResult result = computeSunriseTransitSet(day, location, deltaT, "Sunrise");

        if (result instanceof SunriseResult.RegularDay regularDay) {
            LocalTime sunriseTime = regularDay.sunrise().toLocalTime();
            return new PrayerTime(Prayer.SUNRISE, sunriseTime, false, "SPA sunrise calculation");
        }
        throw nonRegularException(date, Prayer.SUNRISE, result);
    }

    /**
     * Calculates Dhuhr prayer time using astronomical methods.
     * 
     * <p>
     * Dhuhr time is calculated as solar noon (solar transit time) plus one minute.
     * This follows the Islamic rule that Dhuhr begins when the sun starts moving
     * away from its zenith position, not at the exact moment of solar noon.
     * Dhuhr is generally available even at extreme latitudes because solar transit
     * still occurs.
     * </p>
     * 
     * @param date       the date to calculate for
     * @param location   the geographical location
     * @param parameters calculation parameters
     * @return Dhuhr prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    @Override
    public PrayerTime calculateDhuhr(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        return calculateDhuhr(date, location, parameters, DeltaT.estimate(date));
    }

    /**
     * Private method that reuses deltaT to avoid recalculation.
     */
    private PrayerTime calculateDhuhr(LocalDate date, Location location, PrayerCalculationParameters parameters,
            double deltaT) {
        validateInputs(date, location, parameters);

        try {
            ZonedDateTime day = date.atStartOfDay(location.getTimezone());

            SunriseResult result = SPA.calculateSunriseTransitSet(
                    day, location.getLatitude(), location.getLongitude(), deltaT);

            // Add one minute to solar transit time per Islamic rule
            LocalTime dhuhrTime = result.transit().toLocalTime().plusMinutes(1);
            return new PrayerTime(Prayer.DHUHR, dhuhrTime, false, "SPA solar transit calculation + 1 minute");

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate Dhuhr time: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates Asr prayer time using astronomical shadow-length calculations.
     * 
     * <p>
     * Uses either Shafi'i method (shadow = object height) or Hanafi method
     * (shadow = 2 × object height) based on parameters.
     * </p>
     * 
     * @param date       the date to calculate for
     * @param location   the geographical location
     * @param parameters calculation parameters including Asr method
     * @return Asr prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException    if the target elevation is never reached
     *                                  (e.g., during polar day/night)
     */
    @Override
    public PrayerTime calculateAsr(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        return calculateAsr(date, location, parameters, DeltaT.estimate(date));
    }

    /**
     * Private method that reuses deltaT to avoid recalculation.
     */
    private PrayerTime calculateAsr(LocalDate date, Location location, PrayerCalculationParameters parameters,
            double deltaT) {
        validateInputs(date, location, parameters);

        // First get solar noon to calculate base shadow length
        PrayerTime dhuhr = calculateDhuhr(date, location, parameters, deltaT);
        ZonedDateTime solarNoon = date.atTime(dhuhr.getTime()).atZone(location.getTimezone());

        // Calculate solar position at noon to get base shadow length
        SolarPosition noonPosition = computeSolarPosition(solarNoon, location, deltaT, "Asr");

        // Base shadow length at solar noon (for unit object height)
        double noonZenithRad = Math.toRadians(noonPosition.zenithAngle());
        double baseShadowLength = Math.tan(noonZenithRad);

        // Target shadow length based on method
        double targetShadowLength;
        if (parameters.getAsrMethod() == AsrCalculationMethod.HANAFI) {
            targetShadowLength = 2.0 + baseShadowLength; // 2 × object height + noon shadow
        } else {
            targetShadowLength = 1.0 + baseShadowLength; // 1 × object height + noon shadow
        }

        // Convert target shadow length to solar elevation angle
        // shadow_length = tan(zenith_angle), so zenith_angle = arctan(shadow_length)
        double targetZenithRad = Math.atan(targetShadowLength);
        double targetElevationAngle = 90.0 - Math.toDegrees(targetZenithRad);

        // Use SPA to find when sun reaches this elevation angle after noon
        ZonedDateTime day = date.atStartOfDay(location.getTimezone());
        SunriseResult result = computeSunriseTransitSet(day, location, deltaT, targetElevationAngle, "Asr");

        if (result instanceof SunriseResult.RegularDay regularDay) {
            LocalTime asrTime = regularDay.sunset().toLocalTime(); // "sunset" for this elevation = Asr time
            return new PrayerTime(Prayer.ASR, asrTime, false,
                    String.format("SPA shadow calculation (%s method, %.1f° elevation)",
                            parameters.getAsrMethod(), targetElevationAngle));
        }
        throw nonRegularException(date, Prayer.ASR, result);
    }

    /**
     * Calculates Maghrib prayer time using astronomical methods.
     * 
     * <p>
     * Maghrib time is calculated as the moment of sunset.
     * </p>
     * 
     * @param date       the date to calculate for
     * @param location   the geographical location
     * @param parameters calculation parameters
     * @return Maghrib prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    @Override
    public PrayerTime calculateMaghrib(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        return calculateMaghrib(date, location, parameters, DeltaT.estimate(date));
    }

    /**
     * Private method that reuses deltaT to avoid recalculation.
     * 
     * <p>
     * Throws IllegalStateException for polar day/night when sunset does not occur.
     * </p>
     */
    private PrayerTime calculateMaghrib(LocalDate date, Location location, PrayerCalculationParameters parameters,
            double deltaT) {
        validateInputs(date, location, parameters);

        ZonedDateTime day = date.atStartOfDay(location.getTimezone());
        SunriseResult result = computeSunriseTransitSet(day, location, deltaT, "Maghrib");

        if (result instanceof SunriseResult.RegularDay regularDay) {
            LocalTime maghribTime = regularDay.sunset().toLocalTime();
            return new PrayerTime(Prayer.MAGHRIB, maghribTime, false, "SPA sunset calculation");
        }
        throw nonRegularException(date, Prayer.MAGHRIB, result);
    }

    /**
     * Calculates Ishaa prayer time using astronomical methods.
     * 
     * <p>
     * Uses the configured Ishaa angle below the horizon after sunset.
     * </p>
     * 
     * @param date       the date to calculate for
     * @param location   the geographical location
     * @param parameters calculation parameters including Ishaa angle
     * @return Ishaa prayer time
     * @throws IllegalArgumentException if any parameter is null or invalid
     * @throws IllegalStateException    if the sun does not reach the required
     *                                  elevation due to polar day/night
     */
    @Override
    public PrayerTime calculateIshaa(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        return calculateIshaa(date, location, parameters, DeltaT.estimate(date));
    }

    /**
     * Private method that reuses deltaT to avoid recalculation.
     */
    private PrayerTime calculateIshaa(LocalDate date, Location location, PrayerCalculationParameters parameters,
            double deltaT) {
        validateInputs(date, location, parameters);

        // Use negative angle for sunset calculation (sun below horizon)
        double elevationAngle = -parameters.getIshaaAngle();

        ZonedDateTime day = date.atStartOfDay(location.getTimezone());
        SunriseResult result = computeSunriseTransitSet(day, location, deltaT, elevationAngle, "Ishaa");

        if (result instanceof SunriseResult.RegularDay regularDay) {
            LocalTime ishaaTime = regularDay.sunset().toLocalTime();
            return new PrayerTime(Prayer.ISHAA, ishaaTime, false,
                    String.format("SPA astronomical calculation (%.1f°)", parameters.getIshaaAngle()));
        }
        throw nonRegularException(date, Prayer.ISHAA, result);
    }

    /**
     * Calculates Fajr using the Aqrab al-Bilad (nearest area) method for extreme
     * latitudes.
     * 
     * <p>
     * Aqrab al-Bilad means finding the Fajr time from the nearest latitude where
     * the sun actually reaches the target Fajr angle (e.g., -18°). This method is
     * used
     * when normal astronomical calculations are not possible due to polar day/night
     * conditions.
     * </p>
     * 
     * <p>
     * The principle: In high latitude areas, the sun might only reach -14° at its
     * lowest point
     * (solar midnight). At that exact same moment, the sun would be at -18° in a
     * lower
     * latitude area further south. Therefore, solar midnight at the target location
     * gives
     * us the time when the sun reaches the proper Fajr angle at the nearest area
     * where
     * it's astronomically possible.
     * </p>
     * 
     * @param date       the date to calculate for
     * @param location   the geographical location
     * @param parameters calculation parameters
     * @param deltaT     delta T value for astronomical calculations
     * @return Fajr prayer time calculated using Aqrab al-Bilad method
     */
    private PrayerTime calculateFajrAqrabAlBilad(LocalDate date, Location location,
            PrayerCalculationParameters parameters, double deltaT) {
        ZonedDateTime day = date.atStartOfDay(location.getTimezone());

        SunriseResult result = SPA.calculateSunriseTransitSet(
                day, location.getLatitude(), location.getLongitude(), deltaT);

        // Solar midnight = solar noon + 12 hours
        LocalTime solarNoon = result.transit().toLocalTime();
        LocalTime solarMidnight = solarNoon.plusHours(12);

        return new PrayerTime(Prayer.FAJR, solarMidnight, true,
                "AqrabAlBilad method");
    }

    private void validateInputs(LocalDate date, Location location, PrayerCalculationParameters parameters) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
    }

    private SunriseResult computeSunriseTransitSet(ZonedDateTime day, Location location, double deltaT,
            String context) {
        try {
            return SPA.calculateSunriseTransitSet(day, location.getLatitude(), location.getLongitude(), deltaT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate " + context + " time: " + e.getMessage(), e);
        }
    }

    private SunriseResult computeSunriseTransitSet(ZonedDateTime day, Location location, double deltaT,
            double elevationAngle, String context) {
        try {
            return SPA.calculateSunriseTransitSet(day, location.getLatitude(), location.getLongitude(), deltaT,
                    elevationAngle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate " + context + " time: " + e.getMessage(), e);
        }
    }

    private SolarPosition computeSolarPosition(ZonedDateTime time, Location location, double deltaT, String context) {
        try {
            return SPA.calculateSolarPosition(time, location.getLatitude(), location.getLongitude(),
                    location.getElevation(), deltaT, location.getPressure(), location.getTemperature());
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate " + context + " time: " + e.getMessage(), e);
        }
    }

    private IllegalStateException nonRegularException(LocalDate date, Prayer which, SunriseResult result) {
        if (result instanceof SunriseResult.AllDay) {
            return switch (which) {
                case SUNRISE -> new IllegalStateException(
                        "Cannot calculate Sunrise on polar day (sun above horizon all day): " + date);
                case MAGHRIB -> new IllegalStateException(
                        "Cannot calculate Maghrib on polar day (sun never sets): " + date);
                case ISHAA -> new IllegalStateException(
                        "Cannot calculate Ishaa during polar day: required elevation not reached: " + date);
                case ASR -> new IllegalStateException(
                        "Cannot calculate Asr: target elevation never reached during polar day: " + date);
                case FAJR -> new IllegalStateException(
                        "Cannot calculate Fajr during polar day on " + date +
                                ". Consider enabling AqrabAlBilad method.");
                case DHUHR -> new IllegalStateException(
                        "Cannot calculate Dhuhr on polar day: " + date);
            };
        }
        if (result instanceof SunriseResult.AllNight) {
            return switch (which) {
                case SUNRISE -> new IllegalStateException(
                        "Cannot calculate Sunrise on polar night (sun below horizon all day): " + date);
                case MAGHRIB -> new IllegalStateException(
                        "Cannot calculate Maghrib on polar night (sun never rises): " + date);
                case ISHAA -> new IllegalStateException(
                        "Cannot calculate Ishaa during polar night: required elevation not reached: " + date);
                case ASR -> new IllegalStateException(
                        "Cannot calculate Asr: target elevation never reached during polar night: " + date);
                case FAJR -> new IllegalStateException(
                        "Cannot calculate Fajr during polar night on " + date +
                                ". Consider enabling AqrabAlBilad method.");
                case DHUHR -> new IllegalStateException(
                        "Cannot calculate Dhuhr on polar night: " + date);
            };
        }
        return new IllegalStateException("Cannot calculate " + which + " for this location and date: " + date);
    }
}
