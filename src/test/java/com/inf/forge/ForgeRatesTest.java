package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ForgeRatesTest {
    private static final ForgeBalance B = ForgeBalance.DEFAULT;

    @Test
    void damageChance_isEightPercentOfSurvivors() {
        for (int value : new int[] {0, 100, 450, 999}) {
            for (int strikes : new int[] {0, 5, 30}) {
                double broken = ForgeMath.breakChance(value, strikes, B);
                assertEquals(
                        (1.0 - broken) * 0.08,
                        ForgeRates.of(value, strikes, false, B).damageChance(),
                        1e-12);
            }
        }
    }

    @Test
    void greatChance_isFifteenPercentOfRemainder() {
        double broken = ForgeMath.breakChance(240, 12, B);
        double damage = (1.0 - broken) * 0.08;
        assertEquals(
                (1.0 - broken - damage) * 0.15, ForgeRates.of(240, 12, false, B).greatChance(), 1e-12);
    }

    @Test
    void allChances_stayInUnitIntervalAcrossTheGrid() {
        for (int value = 0; value <= 2000; value += 17) {
            for (int strikes = 0; strikes <= 200; strikes += 7) {
                ForgeRates rates = ForgeRates.of(value, strikes, false, B);
                assertInUnitInterval(rates);
            }
        }
    }

    @Test
    void rates_sumToOneAcrossTheGrid() {
        for (int value = 0; value <= 2000; value += 31) {
            for (int strikes = 0; strikes <= 200; strikes += 9) {
                ForgeRates rates = ForgeRates.of(value, strikes, false, B);
                assertEquals(
                        1.0,
                        rates.breakChance() + rates.damageChance() + rates.successChance() + rates.greatChance(),
                        1e-12);
            }
        }
    }

    @Test
    void frenzy_fixesBandsAtSeventyFiveTwentyFive() {
        ForgeRates rates = ForgeRates.of(1000, 500, true, B);
        assertEquals(0.0, rates.breakChance(), 1e-12);
        assertEquals(0.0, rates.damageChance(), 1e-12);
        assertEquals(0.75, rates.successChance(), 1e-12);
        assertEquals(0.25, rates.greatChance(), 1e-12);
    }

    @Test
    void successChance_decreasesAsBreakChanceRises() {
        double previous = 1.0;
        for (int value = 0; value <= 1000; value += 50) {
            ForgeRates rates = ForgeRates.of(value, 20, false, B);
            assertTrue(rates.successChance() < previous, "success at value " + value);
            previous = rates.successChance();
        }
    }

    private static void assertInUnitInterval(ForgeRates rates) {
        for (double chance :
                new double[] {rates.breakChance(), rates.damageChance(), rates.successChance(), rates.greatChance()}) {
            assertTrue(chance >= 0.0 && chance <= 1.0, "chance " + chance);
        }
    }
}
