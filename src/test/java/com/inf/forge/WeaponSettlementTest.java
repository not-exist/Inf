package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WeaponSettlementTest {
    @Test
    void cannotStopWhenOutputOccupied() {
        assertFalse(WeaponSettlement.canStop(500, false));
    }

    @Test
    void cannotStopWithZeroValue() {
        assertFalse(WeaponSettlement.canStop(0, true));
    }

    @Test
    void canStopWithValueAndEmptyOutput() {
        assertTrue(WeaponSettlement.canStop(1, true));
    }
}
