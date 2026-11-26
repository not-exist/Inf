package com.inf.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void thresholds_areStrictlyIncreasing() {
        int previous = -1;
        for (WeaponTier tier : WeaponTier.values()) {
            assertTrue(tier.minValue() > previous, tier.name());
            previous = tier.minValue();
        }
    }

    @Test
    void forValue_atEachThresholdMinusOne_staysAtThePreviousTier() {
        WeaponTier[] tiers = WeaponTier.values();
        for (int index = 1; index < tiers.length; index++) {
            assertEquals(tiers[index - 1], WeaponTier.forValue(tiers[index].minValue() - 1));
            assertEquals(tiers[index], WeaponTier.forValue(tiers[index].minValue()));
        }
    }

    @Test
    void minValues_matchTheDocumentedThresholds() {
        assertEquals(0, WeaponTier.CRUDE.minValue());
        assertEquals(100, WeaponTier.COMMON.minValue());
        assertEquals(240, WeaponTier.FINE.minValue());
        assertEquals(450, WeaponTier.RARE.minValue());
        assertEquals(720, WeaponTier.EPIC.minValue());
        assertEquals(1000, WeaponTier.LEGENDARY.minValue());
    }
}
