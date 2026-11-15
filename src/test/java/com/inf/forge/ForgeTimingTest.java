package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ForgeTimingTest {
    @Test
    void cooldownBlocksUntilElapsed() {
        assertFalse(ForgeTiming.canStrike(100, 100, 4));
        assertFalse(ForgeTiming.canStrike(103, 100, 4));
        assertTrue(ForgeTiming.canStrike(104, 100, 4));
    }

    @Test
    void firstStrikeAlwaysAllowed() {
        assertTrue(ForgeTiming.canStrike(5, Long.MIN_VALUE, 4));
    }
}
