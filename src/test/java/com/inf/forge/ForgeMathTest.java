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
    void rates_sumToOne() {
        ForgeRates rates = ForgeRates.of(420, 18, false, B);
        assertEquals(
                1.0, rates.breakChance() + rates.damageChance() + rates.successChance() + rates.greatChance(), 1e-9);
    }

    @Test
    void frenzyRatesHaveNoBreakOrDamage() {
        ForgeRates rates = ForgeRates.of(420, 18, true, B);
        assertEquals(0.0, rates.breakChance(), 1e-9);
        assertEquals(0.0, rates.damageChance(), 1e-9);
        assertEquals(1.0, rates.successChance() + rates.greatChance(), 1e-9);
    }

    @Test
    void strike_breaksWhenRollIsBelowChance() {
        assertEquals(
                ForgeOutcome.BREAK, ForgeMath.strike(0, 0, 0.0, 50, false, B).outcome());
    }

    @Test
    void strike_damagesInTheDamageBand() {
        ForgeRates rates = ForgeRates.of(100, 10, false, B);
        StrikeOutcome outcome = ForgeMath.strike(100, 10, rates.breakChance(), 50, false, B);
        assertEquals(ForgeOutcome.DAMAGE, outcome.outcome());
        assertTrue(outcome.value() < 100);
        assertEquals(11, outcome.strikes());
        assertTrue(outcome.loss() > 0);
    }

    @Test
    void strike_greatSuccessDoublesGain() {
        ForgeRates rates = ForgeRates.of(100, 10, false, B);
        double roll = rates.breakChance() + rates.damageChance();
        StrikeOutcome outcome = ForgeMath.strike(100, 10, roll, 50, false, B);
        assertEquals(ForgeOutcome.GREAT_SUCCESS, outcome.outcome());
        assertEquals(ForgeMath.valueGain(10, 50, B) * 2, outcome.gain());
        assertEquals(100 + outcome.gain(), outcome.value());
    }

    @Test
    void strike_successAboveAllBands() {
        StrikeOutcome outcome = ForgeMath.strike(100, 10, 0.999, 50, false, B);
        assertEquals(ForgeOutcome.SUCCESS, outcome.outcome());
        assertTrue(outcome.value() > 100);
        assertFalse(outcome.broken());
    }

    @Test
    void strike_frenzyNeverBreaksOrDamages() {
        StrikeOutcome outcome = ForgeMath.strike(0, 0, 0.0, 99, true, B);
        assertEquals(ForgeOutcome.GREAT_SUCCESS, outcome.outcome());
        assertFalse(outcome.broken());
        assertTrue(outcome.value() > 0);
        assertEquals(1, outcome.strikes());
    }

    @Test
    void strike_incrementsStrikes() {
        assertEquals(1, ForgeMath.strike(0, 0, 0.99, 0, false, B).strikes());
    }
}
