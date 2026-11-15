package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HammerDurabilityTest {
    @Test
    void durabilityFloorIsZero() {
        assertEquals(0, HammerMath.remainingAfterStrikes(128, 128));
        assertEquals(1, HammerMath.remainingAfterStrikes(128, 127));
    }

    @Test
    void emptyHammerCannotStrike() {
        assertFalse(HammerMath.canStrike(128, 128));
        assertTrue(HammerMath.canStrike(128, 127));
    }
}
