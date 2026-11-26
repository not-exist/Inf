package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ForgeMathBoundaryTest {
    private static final ForgeBalance B = ForgeBalance.DEFAULT;

    @Test
    void rollExactlyAtBreakChance_isDamage() {
        ForgeRates rates = ForgeRates.of(100, 10, false, B);
        assertEquals(ForgeOutcome.DAMAGE, ForgeMath.strike(100, 10, rates.breakChance(), 50, false, B).outcome());
    }

    @Test
    void rollExactlyAtDamageEdge_isGreatSuccess() {
        ForgeRates rates = ForgeRates.of(100, 10, false, B);
        double edge = rates.breakChance() + rates.damageChance();
        assertEquals(ForgeOutcome.GREAT_SUCCESS, ForgeMath.strike(100, 10, edge, 50, false, B).outcome());
    }

    @Test
    void rollExactlyAtGreatEdge_isSuccess() {
        ForgeRates rates = ForgeRates.of(100, 10, false, B);
        double edge = rates.breakChance() + rates.damageChance() + rates.greatChance();
        assertEquals(ForgeOutcome.SUCCESS, ForgeMath.strike(100, 10, edge, 50, false, B).outcome());
    }

    @Test
    void rollOne_isAlwaysSuccess() {
        assertEquals(ForgeOutcome.SUCCESS, ForgeMath.strike(1000, 100, 1.0, 50, false, B).outcome());
    }

    @Test
    void gainRoll_clampsOutsideZeroToNinetyNine() {
        assertEquals(ForgeMath.valueGain(10, 0, B), ForgeMath.valueGain(10, -5, B));
        assertEquals(ForgeMath.valueGain(10, 99, B), ForgeMath.valueGain(10, 150, B));
    }

    @Test
    void damageLoss_isAtLeastOneAndNeverExceedsValue() {
        for (int value = 0; value <= 500; value += 7) {
            ForgeRates rates = ForgeRates.of(value, 10, false, B);
            StrikeOutcome outcome = ForgeMath.strike(value, 10, rates.breakChance(), 50, false, B);
            assertEquals(ForgeOutcome.DAMAGE, outcome.outcome());
            assertTrue(outcome.loss() >= Math.min(1, value), "loss " + outcome.loss() + " value " + value);
            assertTrue(outcome.loss() <= value, "loss " + outcome.loss() + " value " + value);
        }
    }

    @Test
    void everyNonBreakOutcome_incrementsStrikesOnce() {
        for (double roll = 0.0; roll < 1.0; roll += 0.01) {
            StrikeOutcome outcome = ForgeMath.strike(100, 10, roll, 50, false, B);
            if (outcome.outcome() != ForgeOutcome.BREAK) {
                assertEquals(11, outcome.strikes(), "roll " + roll);
            }
        }
    }

    @Test
    void valueGain_respectsMinGainWithZeroBaseGain() {
        ForgeBalance tiny = ForgeBalance.of(0.0, 0.0, 0.0, 0.85, 0, 0, 0, 4);
        assertEquals(4, ForgeMath.valueGain(0, 0, tiny));
        assertEquals(4, ForgeMath.valueGain(100, 99, tiny));
    }

    @Test
    void breakOutcome_keepsValueAndStrikes() {
        StrikeOutcome outcome = ForgeMath.strike(321, 12, 0.0, 50, false, B);
        assertEquals(ForgeOutcome.BREAK, outcome.outcome());
        assertEquals(321, outcome.value());
        assertEquals(12, outcome.strikes());
        assertTrue(outcome.broken());
        assertFalse(outcome.gain() > 0);
    }
}
