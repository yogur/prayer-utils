package io.github.yogur.prayerutils;

/**
 * Configuration parameters for prayer time calculations.
 */
public class PrayerCalculationParameters {
    private final double fajrAngle;
    private final double ishaaAngle;
    private final AsrCalculationMethod asrMethod;
    private final boolean useAqrabAlBilad;

    private PrayerCalculationParameters(double fajrAngle, double ishaaAngle,
            AsrCalculationMethod asrMethod, boolean useAqrabAlBilad) {
        this.fajrAngle = fajrAngle;
        this.ishaaAngle = ishaaAngle;
        this.asrMethod = asrMethod;
        this.useAqrabAlBilad = useAqrabAlBilad;
    }

    /**
     * Gets the Fajr calculation angle.
     * 
     * @return Angle in degrees below the horizon for Fajr calculation
     */
    public double getFajrAngle() {
        return fajrAngle;
    }

    /**
     * Gets the Ishaa calculation angle.
     * 
     * @return Angle in degrees below the horizon for Ishaa calculation
     */
    public double getIshaaAngle() {
        return ishaaAngle;
    }

    /**
     * Gets the Asr calculation method.
     * 
     * @return The method used for calculating Asr prayer time
     */
    public AsrCalculationMethod getAsrMethod() {
        return asrMethod;
    }

    /**
     * Indicates whether to use Aqrab al-Bilad method for extreme latitudes.
     * 
     * @return true if Aqrab al-Bilad method should be used, false otherwise
     */
    public boolean useAqrabAlBilad() {
        return useAqrabAlBilad;
    }

    /**
     * Creates a builder for prayer calculation parameters.
     * 
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates default parameters with commonly used values.
     * 
     * @return Default prayer calculation parameters
     */
    public static PrayerCalculationParameters createDefault() {
        return builder()
                .fajrAngle(18.0)
                .ishaaAngle(12.0)
                .asrMethod(AsrCalculationMethod.SHAFII)
                .useAqrabAlBilad(true)
                .build();
    }

    /**
     * Helps create prayer time calculation settings.
     *
     * Set the Fajr and Ishaa angles, choose the Asr method, and turn Aqrab
     * al-Bilad on or off. Then call build() to get the final settings.
     */
    public static class Builder {
        private double fajrAngle = 18.0;
        private double ishaaAngle = 18.0;
        private AsrCalculationMethod asrMethod = AsrCalculationMethod.SHAFII;
        private boolean useAqrabAlBilad = true;

        private Builder() {
        }

        /**
         * Sets the Fajr calculation angle in degrees.
         * 
         * @param angle The angle below the horizon for Fajr calculation
         * @return This builder
         */
        public Builder fajrAngle(double angle) {
            if (angle <= 0 || angle >= 90) {
                throw new IllegalArgumentException("Fajr angle must be between 0 and 90 degrees");
            }
            this.fajrAngle = angle;
            return this;
        }

        /**
         * Sets the Ishaa calculation angle in degrees.
         * 
         * @param angle The angle below the horizon for Ishaa calculation
         * @return This builder
         */
        public Builder ishaaAngle(double angle) {
            if (angle <= 0 || angle >= 90) {
                throw new IllegalArgumentException("Ishaa angle must be between 0 and 90 degrees");
            }
            this.ishaaAngle = angle;
            return this;
        }

        /**
         * Sets the Asr calculation method.
         * 
         * @param method The method to use for Asr calculation
         * @return This builder
         */
        public Builder asrMethod(AsrCalculationMethod method) {
            if (method == null) {
                throw new IllegalArgumentException("Asr method cannot be null");
            }
            this.asrMethod = method;
            return this;
        }

        /**
         * Sets whether to use Aqrab al-Bilad method for extreme latitudes.
         * 
         * @param use Whether to use Aqrab al-Bilad method
         * @return This builder
         */
        public Builder useAqrabAlBilad(boolean use) {
            this.useAqrabAlBilad = use;
            return this;
        }

        /**
         * Builds the prayer calculation parameters.
         * 
         * @return The configured parameters
         */
        public PrayerCalculationParameters build() {
            return new PrayerCalculationParameters(fajrAngle, ishaaAngle, asrMethod, useAqrabAlBilad);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        PrayerCalculationParameters that = (PrayerCalculationParameters) obj;
        return Double.compare(that.fajrAngle, fajrAngle) == 0 &&
                Double.compare(that.ishaaAngle, ishaaAngle) == 0 &&
                useAqrabAlBilad == that.useAqrabAlBilad &&
                asrMethod == that.asrMethod;
    }

    @Override
    public int hashCode() {
        long fajrBits = Double.doubleToLongBits(fajrAngle);
        long ishaaBits = Double.doubleToLongBits(ishaaAngle);
        int result = (int) (fajrBits ^ (fajrBits >>> 32));
        result = 31 * result + (int) (ishaaBits ^ (ishaaBits >>> 32));
        result = 31 * result + asrMethod.hashCode();
        result = 31 * result + (useAqrabAlBilad ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("PrayerCalculationParameters{fajr=%.1f°, ishaa=%.1f°, asr=%s, aqrabAlBilad=%s}",
                fajrAngle, ishaaAngle, asrMethod, useAqrabAlBilad);
    }
}