package com.inf.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ForgeMenuRulesTest {
    @Test
    void detachedBlockEntityIsInvalidEvenAtZeroDistance() {
        assertFalse(ForgeMenuRules.stillValid(false, 0.0));
    }

    @Test
    void sameBlockEntityWithinRangeIsValid() {
        assertTrue(ForgeMenuRules.stillValid(true, 64.0));
    }

    @Test
    void sameBlockEntityOutsideRangeIsInvalid() {
        assertFalse(ForgeMenuRules.stillValid(true, 64.01));
    }
}
