package com.inf.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void sameSeed_isDeterministic() {
        BalanceReport first = BalanceSimulator.run(2_000, 7L, ForgeBalance.DEFAULT);
        BalanceReport second = BalanceSimulator.run(2_000, 7L, ForgeBalance.DEFAULT);
        assertEquals(first, second);
    }

    @Test
    void higherRiskLowersLegendaryRateAcrossSeeds() {
        ForgeBalance harsh = ForgeBalance.of(0.015, 0.0024, 0.00003, 0.85, 12, 1, 8, 4);
        for (long seed : new long[] {1L, 2L, 3L, 5L, 8L}) {
            assertTrue(
                    BalanceSimulator.run(5_000, seed, harsh).legendary()
                            < BalanceSimulator.run(5_000, seed, ForgeBalance.DEFAULT)
                                    .legendary(),
                    "seed " + seed);
        }
    }

    @Test
    void maxRiskBreaksOnTheFirstStrike() {
        ForgeBalance certain = ForgeBalance.of(1.0, 0.0, 0.0, 1.0, 12, 1, 8, 4);
        BalanceReport report = BalanceSimulator.run(100, 42L, certain);
        assertEquals(0.0, report.fineOrBetter());
        assertEquals(0.0, report.legendary());
        assertEquals(1.0, report.averageStrikes());
    }

    @Test
    void zeroGainNeverProgresses() {
        ForgeBalance stuck = ForgeBalance.of(0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0);
        BalanceReport report = BalanceSimulator.run(10, 42L, stuck);
        assertEquals(0.0, report.legendary());
        assertTrue(report.averageStrikes() > 0);
    }
}
