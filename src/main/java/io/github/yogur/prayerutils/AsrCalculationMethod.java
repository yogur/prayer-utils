package io.github.yogur.prayerutils;

/**
 * Methods for calculating Asr prayer time, based on different Islamic schools
 * of jurisprudence.
 */
public enum AsrCalculationMethod {
    /**
     * Shafii/Maliki/Hanbali method: Shadow length equals object length.
     */
    SHAFII,

    /**
     * Hanafi method: Shadow length equals twice the object length.
     */
    HANAFI
}