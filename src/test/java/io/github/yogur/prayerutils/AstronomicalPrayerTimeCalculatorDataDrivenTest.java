package io.github.yogur.prayerutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AstronomicalPrayerTimeCalculatorDataDrivenTest {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private final AstronomicalPrayerTimeCalculator calculator = new AstronomicalPrayerTimeCalculator();

    @Test
    @DisplayName("Data-driven: compare calculated values against fixtures (skips if no fixtures or expected missing)")
    void dataDriven() throws IOException {
        List<DataFixtures.CaseFile> files = new ArrayList<>();

        DataFixtures.CaseFile normal = tryReadJson("/fixtures/normal_cities.json");
        DataFixtures.CaseFile extreme = tryReadJson("/fixtures/extreme_latitudes_aqrab.json");
        if (normal != null) files.add(normal);
        if (extreme != null) files.add(extreme);

        assumeTrue(!files.isEmpty(), "No fixtures present; skipping data-driven assertions.");

        for (DataFixtures.CaseFile file : files) {
            for (DataFixtures.Case c : file.cases) {
                LocalDate date = DataFixtures.parseDate(c.input.date);
                ZoneId zone = DataFixtures.parseZone(c.input.timezone);
                Location loc = new Location(c.input.latitude, c.input.longitude, c.input.elevation, zone);
                PrayerCalculationParameters params = PrayerCalculationParameters.builder()
                        .fajrAngle(c.input.fajrAngle)
                        .ishaaAngle(c.input.ishaaAngle)
                        .asrMethod(AsrCalculationMethod.valueOf(c.input.asrMethod))
                        .useAqrabAlBilad(c.input.useAqrabAlBilad)
                        .build();

                PrayerTimes times = calculator.calculatePrayerTimes(date, loc, params);

                if (notBlank(c.expected.fajr)) {
                    assertEquals(parseHm(c.expected.fajr), toHm(times.getFajr().getTime()),
                            () -> "Fajr mismatch for " + file.description);
                }
                if (notBlank(c.expected.sunrise)) {
                    assertEquals(parseHm(c.expected.sunrise), toHm(times.getSunrise().getTime()),
                            () -> "Sunrise mismatch for " + file.description);
                }
                if (notBlank(c.expected.dhuhr)) {
                    assertEquals(parseHm(c.expected.dhuhr), toHm(times.getDhuhr().getTime()),
                            () -> "Dhuhr mismatch for " + file.description);
                }
                if (notBlank(c.expected.asr)) {
                    assertEquals(parseHm(c.expected.asr), toHm(times.getAsr().getTime()),
                            () -> "Asr mismatch for " + file.description);
                }
                if (notBlank(c.expected.maghrib)) {
                    assertEquals(parseHm(c.expected.maghrib), toHm(times.getMaghrib().getTime()),
                            () -> "Maghrib mismatch for " + file.description);
                }
                if (notBlank(c.expected.ishaa)) {
                    assertEquals(parseHm(c.expected.ishaa), toHm(times.getIshaa().getTime()),
                            () -> "Ishaa mismatch for " + file.description);
                }
            }
        }
    }

    private static DataFixtures.CaseFile tryReadJson(String resourcePath) throws IOException {
        try (InputStream is = AstronomicalPrayerTimeCalculatorDataDrivenTest.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            return MAPPER.readValue(is, DataFixtures.CaseFile.class);
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank() && !"--".equals(s.trim());
    }

    private static String toHm(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private static String parseHm(String s) {
        LocalTime t = LocalTime.parse(s);
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}


