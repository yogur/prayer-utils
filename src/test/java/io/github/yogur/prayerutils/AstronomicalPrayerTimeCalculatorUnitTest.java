package io.github.yogur.prayerutils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AstronomicalPrayerTimeCalculatorUnitTest {

    private final AstronomicalPrayerTimeCalculator calculator = new AstronomicalPrayerTimeCalculator();

    @Test
    @DisplayName("calculatePrayerTimes: null validation")
    void calculatePrayerTimes_nulls() {
        Location loc = new Location(0, 0);
        PrayerCalculationParameters params = PrayerCalculationParameters.createDefault();

        assertThrows(IllegalArgumentException.class, () -> calculator.calculatePrayerTimes(null, loc, params));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculatePrayerTimes(LocalDate.now(), null, params));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculatePrayerTimes(LocalDate.now(), loc, null));
    }

    @Test
    @DisplayName("Extreme latitude: Sunrise and Maghrib throw on polar day")
    void sunriseAndMaghrib_extremeLatitude_throw() {
        LocalDate date = LocalDate.of(2025, 6, 21);
        Location tromso = new Location(69.6492, 18.9553, 0, ZoneId.of("Europe/Oslo"));
        PrayerCalculationParameters params = PrayerCalculationParameters.createDefault();

        assertThrows(IllegalStateException.class, () ->
                calculator.calculatePrayerTimes(date, tromso, params).getSunrise());

        assertThrows(IllegalStateException.class, () ->
                calculator.calculateMaghrib(date, tromso, params));
    }

    @Test
    @DisplayName("Extreme latitude with Aqrab al-Bilad: Ishaa still throws (no fallback)")
    void ishaa_extreme_withAqrabAlBiladStillThrows() {
        LocalDate date = LocalDate.of(2025, 6, 21);
        Location tromso = new Location(69.6492, 18.9553, 0, ZoneId.of("Europe/Oslo"));
        PrayerCalculationParameters params = PrayerCalculationParameters.builder()
                .fajrAngle(18.0)
                .ishaaAngle(18.0)
                .asrMethod(AsrCalculationMethod.SHAFII)
                .useAqrabAlBilad(true)
                .build();

        assertThrows(IllegalStateException.class, () -> calculator.calculateIshaa(date, tromso, params));
    }

    @Test
    @DisplayName("Individual prayers compute without exception for a normal location")
    void individualPrayers_normalLocation() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        Location berlin = new Location(52.52, 13.405, 34, ZoneId.of("Europe/Berlin"));
        PrayerCalculationParameters params = PrayerCalculationParameters.builder()
                .fajrAngle(18.0)
                .ishaaAngle(12.0)
                .asrMethod(AsrCalculationMethod.SHAFII)
                .useAqrabAlBilad(true)
                .build();

        assertNotNull(calculator.calculateFajr(date, berlin, params));
        assertNotNull(calculator.calculateDhuhr(date, berlin, params));
        assertNotNull(calculator.calculateAsr(date, berlin, params));
        assertNotNull(calculator.calculateMaghrib(date, berlin, params));
        assertNotNull(calculator.calculateIshaa(date, berlin, params));
    }

    @Test
    @DisplayName("Extreme latitude without Aqrab al-Bilad: Fajr should throw when not computable")
    void extremeLatitude_noAqrabAlBilad_throws() {
        // Tromsø, Norway in summer (midnight sun) — around June 21 the sun doesn't set
        LocalDate date = LocalDate.of(2025, 6, 21);
        Location tromso = new Location(69.6492, 18.9553, 0, ZoneId.of("Europe/Oslo"));
        PrayerCalculationParameters params = PrayerCalculationParameters.builder()
                .fajrAngle(18.0)
                .ishaaAngle(18.0)
                .asrMethod(AsrCalculationMethod.SHAFII)
                .useAqrabAlBilad(false)
                .build();

        assertThrows(IllegalStateException.class, () -> calculator.calculateFajr(date, tromso, params));
        assertThrows(IllegalStateException.class, () -> calculator.calculateIshaa(date, tromso, params));
    }

    @Test
    @DisplayName("Extreme latitude with Aqrab al-Bilad: Fajr falls back and is flagged")
    void extremeLatitude_withAqrabAlBilad_fallsBack() {
        LocalDate date = LocalDate.of(2025, 6, 21);
        Location tromso = new Location(69.6492, 18.9553, 0, ZoneId.of("Europe/Oslo"));
        PrayerCalculationParameters params = PrayerCalculationParameters.builder()
                .fajrAngle(18.0)
                .ishaaAngle(18.0)
                .asrMethod(AsrCalculationMethod.SHAFII)
                .useAqrabAlBilad(true)
                .build();

        PrayerTime fajr = calculator.calculateFajr(date, tromso, params);
        assertNotNull(fajr);
        assertTrue(fajr.isAqrabAlBiladCalculation());
        assertEquals(Prayer.FAJR, fajr.getPrayer());
        assertNotNull(fajr.getTime());
    }

    @Test
    @DisplayName("Asr method switch affects calculation path")
    void asrMethodSwitch() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        Location berlin = new Location(52.52, 13.405, 34, ZoneId.of("Europe/Berlin"));

        PrayerCalculationParameters shafii = PrayerCalculationParameters.builder()
                .asrMethod(AsrCalculationMethod.SHAFII)
                .fajrAngle(18.0)
                .ishaaAngle(12.0)
                .useAqrabAlBilad(true)
                .build();

        PrayerCalculationParameters hanafi = PrayerCalculationParameters.builder()
                .asrMethod(AsrCalculationMethod.HANAFI)
                .fajrAngle(18.0)
                .ishaaAngle(12.0)
                .useAqrabAlBilad(true)
                .build();

        PrayerTime asrShafii = calculator.calculateAsr(date, berlin, shafii);
        PrayerTime asrHanafi = calculator.calculateAsr(date, berlin, hanafi);

        assertNotNull(asrShafii.getTime());
        assertNotNull(asrHanafi.getTime());

        // Hanafi Asr should be later or equal compared to Shafii (stricter shadow condition)
        assertTrue(!asrHanafi.getTime().isBefore(asrShafii.getTime()));
    }
}


