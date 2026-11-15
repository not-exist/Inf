package com.inf.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WeaponTierTest {
    @Test
    void forValue_hitsExactThresholds() {
        assertEquals(WeaponTier.CRUDE, WeaponTier.forValue(0));
        assertEquals(WeaponTier.COMMON, WeaponTier.forValue(100));
        assertEquals(WeaponTier.FINE, WeaponTier.forValue(240));
        assertEquals(WeaponTier.RARE, WeaponTier.forValue(450));
        assertEquals(WeaponTier.EPIC, WeaponTier.forValue(720));
        assertEquals(WeaponTier.LEGENDARY, WeaponTier.forValue(1000));
    }

    @Test
    void forValue_justBelowThresholdStaysLower() {
        assertEquals(WeaponTier.CRUDE, WeaponTier.forValue(99));
        assertEquals(WeaponTier.COMMON, WeaponTier.forValue(239));
    }

    @Test
    void forValue_neverExceedsLegendary() {
        assertEquals(WeaponTier.LEGENDARY, WeaponTier.forValue(Integer.MAX_VALUE));
    }
}
