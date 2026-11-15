package com.inf.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inf.forge.ForgeBalance;
import org.junit.jupiter.api.Test;

class BalanceSimulationTest {
    @Test
    void defaultBalance_landsInDocumentedBand() {
        BalanceReport report = BalanceSimulator.run(100_000, 42L, ForgeBalance.DEFAULT);
        assertTrue(report.fineOrBetter() > 0.20, "fine rate " + report.fineOrBetter());
        assertTrue(report.legendary() > 0.005 && report.legendary() < 0.20, "legendary " + report.legendary());
        assertTrue(report.averageStrikes() > 20 && report.averageStrikes() < 45, "strikes " + report.averageStrikes());
    }

    @Test
    void higherRiskLowersLegendaryRate() {
        ForgeBalance harsh = ForgeBalance.of(0.015, 0.0024, 0.00003, 0.85, 12, 1, 8, 4);
        assertTrue(BalanceSimulator.run(100_000, 42L, harsh).legendary()
                < BalanceSimulator.run(100_000, 42L, ForgeBalance.DEFAULT).legendary());
    }
}
