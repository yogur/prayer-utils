# Prayer Utils

[![Maven](https://img.shields.io/maven-central/v/io.github.yogur/prayer-utils?color=dodgerblue)](https://central.sonatype.com/artifact/io.github.yogur/prayer-utils/)
[![javadoc](https://javadoc.io/badge2/io.github.yogur/prayer-utils/javadoc.svg)](https://javadoc.io/doc/io.github.yogur/prayer-utils)

High-precision Java library for calculating Islamic prayer times.

This library computes Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Ishaa using the NREL Solar Position Algorithm (SPA) under the hood for accurate astronomical results. It supports configurable angles for Fajr/Ishaa, both Shafi'i and Hanafi Asr methods, and a safe fallback for extreme latitudes (Aqrab al-Bilad) where conventional calculations are not possible.

## Features

- Accurate astronomical calculations (SPA-based)
- Configurable Fajr and Ishaa angles
- Asr calculation methods: Shafi'i or Hanafi
- Handles timezones via IANA zone IDs (e.g., "Europe/Berlin")
- Optional Aqrab al-Bilad fallback for high latitudes (Fajr)
- Simple, immutable model types for results

## Requirements

- Java 17+

## Installation

### Maven

```xml
<dependency>
  <groupId>io.github.yogur</groupId>
  <artifactId>prayer-utils</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle (Groovy DSL)

```groovy
dependencies {
  implementation "io.github.yogur:prayer-utils:1.0.0"
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
  implementation("io.github.yogur:prayer-utils:1.0.0")
}
```

## Quick start

Calculate all prayer times for a day and location:

```java
import io.github.yogur.prayerutils.*;
import java.time.LocalDate;
import java.time.ZoneId;

public class Example {
  public static void main(String[] args) {
    PrayerTimeCalculator calculator = new AstronomicalPrayerTimeCalculator();

    LocalDate date = LocalDate.of(2025, 6, 1);
    Location berlin = new Location(52.52, 13.405, 34, ZoneId.of("Europe/Berlin"));

    PrayerCalculationParameters params = PrayerCalculationParameters.builder()
        .fajrAngle(18.0)
        .ishaaAngle(12.0)
        .asrMethod(AsrCalculationMethod.SHAFII)
        .useAqrabAlBilad(true)
        .build();

    PrayerTimes times = calculator.calculatePrayerTimes(date, berlin, params);

    System.out.println("Fajr:    " + times.getFajr().getTime());
    System.out.println("Sunrise: " + times.getSunrise().getTime());
    System.out.println("Dhuhr:   " + times.getDhuhr().getTime());
    System.out.println("Asr:     " + times.getAsr().getTime());
    System.out.println("Maghrib: " + times.getMaghrib().getTime());
    System.out.println("Ishaa:   " + times.getIshaa().getTime());
  }
}
```

Get a single prayer time (e.g., Asr) with Hanafi method:

```java
PrayerCalculationParameters hanafi = PrayerCalculationParameters.builder()
    .fajrAngle(18.0)
    .ishaaAngle(12.0)
    .asrMethod(AsrCalculationMethod.HANAFI)
    .useAqrabAlBilad(true)
    .build();

PrayerTime asr = calculator.calculateAsr(date, berlin, hanafi);
System.out.println("Asr (Hanafi): " + asr.getTime());
```

### Important usage notice

In Islamic practice, the start and end of prayer times are based on direct observation (e.g., true dawn, sunset, and disappearance of twilight). Computed schedules are for reference, and they may differ slightly due to local conditions such as atmospheric refraction, weather, terrain/horizon height, and other environmental factors.

- Consider adding a small safety margin: add up to 5 minutes to the computed start time before praying.
- Avoid timing prayers at the very end of their window.
- Where available, prefer locally observed timetables from trustworthy people.

### Using the PrayerTimeCalculator interface (custom providers)

The library exposes the `PrayerTimeCalculator` interface so you can plug in alternative providers. For example, if you maintain a database of prayer times for specific cities based on real observation, you can implement the interface to return those values directly.

Example: a calculator backed by a database of observed times, with astronomical fallback when no record exists.

```java
import io.github.yogur.prayerutils.*;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

class DatabasePrayerTimeCalculator implements PrayerTimeCalculator {
  private final ObservedPrayerRepository repo;

  DatabasePrayerTimeCalculator(ObservedPrayerRepository repo) {
    this.repo = repo;
  }

  @Override
  public PrayerTimes calculatePrayerTimes(LocalDate date, Location location, PrayerCalculationParameters params) {
    ObservedDay d = repo.findByDateAndLocation(date, location.getTimezone());
    if (d == null) throw new IllegalStateException("No observed timetable available");

    Map<Prayer, PrayerTime> times = new EnumMap<>(Prayer.class);
    times.put(Prayer.FAJR,    new PrayerTime(Prayer.FAJR,    d.getFajr(),    false, "Observed (DB)"));
    times.put(Prayer.SUNRISE, new PrayerTime(Prayer.SUNRISE, d.getSunrise(), false, "Observed (DB)"));
    times.put(Prayer.DHUHR,   new PrayerTime(Prayer.DHUHR,   d.getDhuhr(),   false, "Observed (DB)"));
    times.put(Prayer.ASR,     new PrayerTime(Prayer.ASR,     d.getAsr(),     false, "Observed (DB)"));
    times.put(Prayer.MAGHRIB, new PrayerTime(Prayer.MAGHRIB, d.getMaghrib(), false, "Observed (DB)"));
    times.put(Prayer.ISHAA,   new PrayerTime(Prayer.ISHAA,   d.getIshaa(),   false, "Observed (DB)"));
    return new PrayerTimes(date, location, times);
  }

  // Implement the single-prayer methods by delegating to calculatePrayerTimes(...)
  @Override public PrayerTime calculateFajr(LocalDate date, Location loc, PrayerCalculationParameters p) {
    return calculatePrayerTimes(date, loc, p).getFajr();
  }
  @Override public PrayerTime calculateDhuhr(LocalDate date, Location loc, PrayerCalculationParameters p) {
    return calculatePrayerTimes(date, loc, p).getDhuhr();
  }
  @Override public PrayerTime calculateAsr(LocalDate date, Location loc, PrayerCalculationParameters p) {
    return calculatePrayerTimes(date, loc, p).getAsr();
  }
  @Override public PrayerTime calculateMaghrib(LocalDate date, Location loc, PrayerCalculationParameters p) {
    return calculatePrayerTimes(date, loc, p).getMaghrib();
  }
  @Override public PrayerTime calculateIshaa(LocalDate date, Location loc, PrayerCalculationParameters p) {
    return calculatePrayerTimes(date, loc, p).getIshaa();
  }
}

// Usage: try observed timetable first; fallback to astronomical if missing
PrayerTimeCalculator observed = new DatabasePrayerTimeCalculator(repo);
PrayerTimeCalculator astro = new AstronomicalPrayerTimeCalculator();

PrayerTimes result;
try {
  result = observed.calculatePrayerTimes(date, berlin, params);
} catch (IllegalStateException noData) {
  result = astro.calculatePrayerTimes(date, berlin, params);
}
```

## Extreme latitudes

At very high latitudes during polar day/night, conventional calculations may be impossible for some prayers.

- When enabled via `.useAqrabAlBilad(true)`, the library falls back to Aqrab al-Bilad for Fajr. The returned `PrayerTime` will be flagged with `isAqrabAlBiladCalculation() == true`.
- Ishaa has no fallback in this library and may throw an `IllegalStateException` during polar day/night.
- Sunrise and Maghrib may throw on days when the sun never rises/sets.

```java
Location tromso = new Location(69.6492, 18.9553, 0, ZoneId.of("Europe/Oslo"));
PrayerCalculationParameters params = PrayerCalculationParameters.builder()
    .fajrAngle(18.0)
    .ishaaAngle(18.0)
    .asrMethod(AsrCalculationMethod.SHAFII)
    .useAqrabAlBilad(true) // enable fallback for Fajr
    .build();

PrayerTime fajr = calculator.calculateFajr(LocalDate.of(2025, 6, 21), tromso, params);
if (fajr.isAqrabAlBiladCalculation()) {
  System.out.println("Fajr used Aqrab al-Bilad fallback: " + fajr.getTime());
}
```

## API at a glance

- `PrayerTimeCalculator`: Interface for computing prayer times.
- `AstronomicalPrayerTimeCalculator`: Default SPA-based implementation.
- `Location`: Holds latitude, longitude, elevation, atmospheric conditions, and timezone.
- `PrayerCalculationParameters`: Builder-driven configuration (Fajr/Ishaa angles, Asr method, Aqrab al-Bilad).
- `AsrCalculationMethod`: `SHAFII` or `HANAFI`.
- `Prayer`: Enum of prayers plus `SUNRISE` convenience value.
- `PrayerTimes`: Immutable container with getters for each prayer.
- `PrayerTime`: Contains the time, prayer type, and flags/description of the calculation method used.

## Notes

- Always provide a correct IANA timezone (e.g., `ZoneId.of("Europe/Berlin")`) for accurate local times.
- `Location` defaults to sea-level and standard conditions if you only pass latitude/longitude.
- Invalid inputs throw `IllegalArgumentException`. In extreme latitude scenarios where a time cannot be computed, an `IllegalStateException` is thrown (unless a fallback is available and enabled).
