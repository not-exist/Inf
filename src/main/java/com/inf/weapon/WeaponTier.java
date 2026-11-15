package com.inf.weapon;

import java.util.Locale;

public enum WeaponTier {
    CRUDE(0),
    COMMON(100),
    FINE(240),
    RARE(450),
    EPIC(720),
    LEGENDARY(1000);

    private final int minValue;

    WeaponTier(int minValue) {
        this.minValue = minValue;
    }

    public int minValue() {
        return minValue;
    }

    public String translationKey() {
        return "inf.tier." + name().toLowerCase(Locale.ROOT);
    }

    public static WeaponTier forValue(int value) {
        WeaponTier result = CRUDE;
        for (WeaponTier tier : values()) {
            if (value >= tier.minValue) {
                result = tier;
            }
        }
        return result;
    }
}
