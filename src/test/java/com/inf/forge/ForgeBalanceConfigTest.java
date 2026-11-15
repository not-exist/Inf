package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ForgeBalanceConfigTest {
    @Test
    void of_preservesArgumentOrder() {
        ForgeBalance balance = ForgeBalance.of(0.02, 0.003, 0.00004, 0.9, 10, 2, 6, 3);
        assertEquals(0.02, balance.baseRisk(), 1e-9);
        assertEquals(0.003, balance.riskPerStrike(), 1e-9);
        assertEquals(0.00004, balance.riskPerValue(), 1e-9);
        assertEquals(0.9, balance.maxRisk(), 1e-9);
        assertEquals(10, balance.baseGain());
        assertEquals(2, balance.gainPerStrike());
        assertEquals(6, balance.gainVariance());
        assertEquals(3, balance.minGain());
    }
}
