package io.github.yogur.prayerutils;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Represents a geographical location for prayer time calculations.
 */
public class Location {
    private final double latitude;
    private final double longitude;
    private final double elevation;
    private final double pressure;
    private final double temperature;
    private final ZoneId timezone;

    /**
     * Creates a location with elevation at sea level and standard atmospheric
     * conditions.
     * Uses UTC timezone by default.
     * 
     * @param latitude  Latitude in decimal degrees (-90 to 90)
     * @param longitude Longitude in decimal degrees (-180 to 180)
     */
    public Location(double latitude, double longitude) {
        this(latitude, longitude, 0.0);
    }

    /**
     * Creates a location with specified elevation and standard atmospheric
     * conditions.
     * Uses UTC timezone by default.
     * 
     * @param latitude  Latitude in decimal degrees (-90 to 90)
     * @param longitude Longitude in decimal degrees (-180 to 180)
     * @param elevation Elevation above sea level in meters
     */
    public Location(double latitude, double longitude, double elevation) {
        this(latitude, longitude, elevation, 1010.0, 15.0);
    }

    /**
     * Creates a location with specified elevation, atmospheric conditions, and
     * timezone.
     * 
     * @param latitude  Latitude in decimal degrees (-90 to 90)
     * @param longitude Longitude in decimal degrees (-180 to 180)
     * @param elevation Elevation above sea level in meters
     * @param timezone  Timezone for this location
     */
    public Location(double latitude, double longitude, double elevation, ZoneId timezone) {
        this(latitude, longitude, elevation, 1010.0, 15.0, timezone);
    }

    /**
     * Creates a location with specified elevation and atmospheric conditions.
     * Uses UTC timezone by default.
     * 
     * @param latitude    Latitude in decimal degrees (-90 to 90)
     * @param longitude   Longitude in decimal degrees (-180 to 180)
     * @param elevation   Elevation above sea level in meters
     * @param pressure    Atmospheric pressure in hectopascals (hPa)
     * @param temperature Temperature in degrees Celsius
     */
    public Location(double latitude, double longitude, double elevation, double pressure, double temperature) {
        this(latitude, longitude, elevation, pressure, temperature, ZoneOffset.UTC);
    }

    /**
     * Creates a location with specified elevation, atmospheric conditions, and
     * timezone.
     * 
     * @param latitude    Latitude in decimal degrees (-90 to 90)
     * @param longitude   Longitude in decimal degrees (-180 to 180)
     * @param elevation   Elevation above sea level in meters
     * @param pressure    Atmospheric pressure in hectopascals (hPa)
     * @param temperature Temperature in degrees Celsius
     * @param timezone    Timezone for this location
     */
    public Location(double latitude, double longitude, double elevation, double pressure, double temperature,
            ZoneId timezone) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
        if (pressure < 0 || pressure > 3000) {
            throw new IllegalArgumentException("Pressure must be between 0 and 3000 hPa");
        }
        if (temperature < -273 || temperature > 273) {
            throw new IllegalArgumentException("Temperature must be between -273 and 273 degrees Celsius");
        }
        if (timezone == null) {
            throw new IllegalArgumentException("Timezone cannot be null");
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.pressure = pressure;
        this.temperature = temperature;
        this.timezone = timezone;
    }

    /**
     * Gets the latitude in decimal degrees.
     * 
     * @return Latitude (-90 to 90 degrees)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude in decimal degrees.
     * 
     * @return Longitude (-180 to 180 degrees)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Gets the elevation above sea level.
     * 
     * @return Elevation in meters
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Gets the atmospheric pressure.
     * 
     * @return Pressure in hectopascals (hPa)
     */
    public double getPressure() {
        return pressure;
    }

    /**
     * Gets the temperature.
     * 
     * @return Temperature in degrees Celsius
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Gets the timezone for this location.
     * 
     * @return The timezone
     */
    public ZoneId getTimezone() {
        return timezone;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Location location = (Location) obj;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0 &&
                Double.compare(location.elevation, elevation) == 0 &&
                Double.compare(location.pressure, pressure) == 0 &&
                Double.compare(location.temperature, temperature) == 0 &&
                timezone.equals(location.timezone);
    }

    @Override
    public int hashCode() {
        long latBits = Double.doubleToLongBits(latitude);
        long lonBits = Double.doubleToLongBits(longitude);
        long elevBits = Double.doubleToLongBits(elevation);
        long pressBits = Double.doubleToLongBits(pressure);
        long tempBits = Double.doubleToLongBits(temperature);
        return (int) (latBits ^ (latBits >>> 32) ^ lonBits ^ (lonBits >>> 32) ^
                elevBits ^ (elevBits >>> 32) ^ pressBits ^ (pressBits >>> 32) ^
                tempBits ^ (tempBits >>> 32) ^ timezone.hashCode());
    }

    @Override
    public String toString() {
        return String.format(
                "Location{lat=%.6f, lon=%.6f, elevation=%.1fm, pressure=%.1fhPa, temp=%.1f°C, timezone=%s}",
                latitude, longitude, elevation, pressure, temperature, timezone);
    }
}