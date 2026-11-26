package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HammerEdgeCaseTest {
    @Test
    void remaining_clampsAtZeroForOverUse() {
        assertEquals(0, HammerMath.remainingAfterStrikes(128, 130));
        assertEquals(0, HammerMath.remainingAfterStrikes(0, 0));
    }

    @Test
    void canStrike_isFalseWhenDamageExceedsMax() {
        assertFalse(HammerMath.canStrike(128, 200));
        assertTrue(HammerMath.canStrike(1, 0));
    }

    @Test
    void zeroCooldownAlwaysAllows() {
        assertTrue(ForgeTiming.canStrike(100, 100, 0));
        assertTrue(ForgeTiming.canStrike(100, 100, -1));
    }

    @Test
    void cooldownUsesTheStrikeTimestamp() {
        assertFalse(ForgeTiming.canStrike(104, 103, 4), "one tick is not enough");
        assertFalse(ForgeTiming.canStrike(100, 104, 4), "a future timestamp keeps blocking");
        assertTrue(ForgeTiming.canStrike(108, 104, 4), "four ticks is enough");
    }
}
