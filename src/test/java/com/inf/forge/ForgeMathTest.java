package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ForgeMathTest {
    private static final ForgeBalance B = ForgeBalance.DEFAULT;

    @Test
    void breakChance_atOriginIsBaseRisk() {
        assertEquals(0.015, ForgeMath.breakChance(0, 0, B), 1e-9);
    }

    @Test
    void breakChance_growsWithStrikesAndValue() {
        double a = ForgeMath.breakChance(0, 0, B);
        double b = ForgeMath.breakChance(0, 10, B);
        double c = ForgeMath.breakChance(100, 10, B);
        assertTrue(a < b && b < c);
    }

    @Test
    void breakChance_capsAtMaxRisk() {
        assertEquals(0.85, ForgeMath.breakChance(1_000_000, 10_000, B), 1e-9);
    }

    @Test
    void valueGain_staysWithinDocumentedRange() {
        for (int roll = 0; roll < 100; roll++) {
            for (int strikes = 0; strikes < 60; strikes++) {
                int gain = ForgeMath.valueGain(strikes, roll, B);
                assertTrue(gain >= 4 && gain <= 30, "gain " + gain);
            }
        }
    }

    @Test
    void strike_breaksWhenRollIsBelowChance() {
        assertTrue(ForgeMath.strike(0, 0, 0.0, 50, false, B).broken());
    }

    @Test
    void strike_survivesWhenRollEqualsChance() {
        double risk = ForgeMath.breakChance(0, 0, B);
        assertFalse(ForgeMath.strike(0, 0, risk, 50, false, B).broken());
    }

    @Test
    void strike_frenzyNeverBreaksButStillGains() {
        StrikeOutcome outcome = ForgeMath.strike(0, 0, 0.0, 99, true, B);
        assertFalse(outcome.broken());
        assertTrue(outcome.value() > 0);
        assertEquals(1, outcome.strikes());
    }

    @Test
    void strike_incrementsStrikes() {
        assertEquals(1, ForgeMath.strike(0, 0, 0.99, 0, false, B).strikes());
    }
}
