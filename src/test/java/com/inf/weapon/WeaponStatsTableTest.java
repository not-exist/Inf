package com.inf.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WeaponStatsTableTest {
    @Test
    void sword_endpointsMatchDesign() {
        assertEquals(
                4.0,
                WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.CRUDE).attackDamage(),
                1e-9);
        assertEquals(
                11.0,
                WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.LEGENDARY).attackDamage(),
                1e-9);
        assertEquals(
                -2.4,
                WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.CRUDE).attackSpeedModifier(),
                1e-9);
        assertEquals(
                -2.0,
                WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.LEGENDARY).attackSpeedModifier(),
                1e-9);
    }

    @Test
    void dagger_hasNoSweepAndNegativeReach() {
        WeaponStats stats = WeaponStatsTable.forTier(WeaponKind.DAGGER, WeaponTier.LEGENDARY);
        assertEquals(0.0, stats.sweepRatio(), 1e-9);
        assertEquals(-0.5, stats.reachModifier(), 1e-9);
        assertEquals(3.0, 4.0 + stats.attackSpeedModifier(), 1e-9);
    }

    @Test
    void scythe_hasLongReachAndHighSweep() {
        WeaponStats stats = WeaponStatsTable.forTier(WeaponKind.SCYTHE, WeaponTier.LEGENDARY);
        assertEquals(1.5, stats.reachModifier(), 1e-9);
        assertEquals(1.2, stats.sweepRatio(), 1e-9);
    }

    @Test
    void pickaxe_harvestLevelRisesToDiamond() {
        assertEquals(
                1,
                WeaponStatsTable.forTier(WeaponKind.PICKAXE, WeaponTier.CRUDE).harvestLevel());
        assertEquals(
                4,
                WeaponStatsTable.forTier(WeaponKind.PICKAXE, WeaponTier.LEGENDARY)
                        .harvestLevel());
        assertEquals(
                15.0,
                WeaponStatsTable.forTier(WeaponKind.PICKAXE, WeaponTier.LEGENDARY)
                        .miningSpeed(),
                1e-9);
    }

    @Test
    void everyKind_scalesDamageMonotonically() {
        for (WeaponKind kind : WeaponKind.values()) {
            double previous = -1;
            for (WeaponTier tier : WeaponTier.values()) {
                double damage = WeaponStatsTable.forTier(kind, tier).attackDamage();
                assertTrue(damage > previous, kind + " " + tier);
                previous = damage;
            }
        }
    }

    @Test
    void tableDefinesEveryKindAndTier() {
        for (WeaponKind kind : WeaponKind.values()) {
            for (WeaponTier tier : WeaponTier.values()) {
                WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
                assertTrue(stats.attackDamage() > 0, kind + " " + tier);
                assertTrue(stats.attackSpeedModifier() <= 0, kind + " " + tier);
                assertTrue(stats.harvestLevel() <= 4, kind + " " + tier);
                assertTrue(stats.miningSpeed() >= 0, kind + " " + tier);
            }
        }
    }

    @Test
    void miningStats_areZeroOutsideToolKinds() {
        for (WeaponKind kind : WeaponKind.values()) {
            boolean tool = kind == WeaponKind.PICKAXE || kind == WeaponKind.AXE;
            for (WeaponTier tier : WeaponTier.values()) {
                WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
                if (!tool) {
                    assertEquals(0.0, stats.miningSpeed(), 1e-9, kind + " " + tier);
                    assertEquals(0, stats.harvestLevel(), kind + " " + tier);
                }
            }
        }
    }

    @Test
    void miningSpeedAndHarvest_areMonotonicForToolKinds() {
        for (WeaponKind kind : new WeaponKind[] {WeaponKind.PICKAXE, WeaponKind.AXE}) {
            double previousSpeed = -1;
            int previousHarvest = -1;
            for (WeaponTier tier : WeaponTier.values()) {
                WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
                assertTrue(stats.miningSpeed() > previousSpeed, kind + " " + tier);
                assertTrue(stats.harvestLevel() >= previousHarvest, kind + " " + tier);
                previousSpeed = stats.miningSpeed();
                previousHarvest = stats.harvestLevel();
            }
        }
    }

    @Test
    void sweepIsZeroForNonSweepingKinds_andReachSignIsKindSpecific() {
        for (WeaponKind kind : WeaponKind.values()) {
            for (WeaponTier tier : WeaponTier.values()) {
                WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
                if (kind != WeaponKind.SWORD && kind != WeaponKind.SCYTHE) {
                    assertEquals(0.0, stats.sweepRatio(), 1e-9, kind + " " + tier);
                }
                if (kind == WeaponKind.DAGGER) {
                    assertTrue(stats.reachModifier() < 0, kind + " " + tier);
                } else if (kind == WeaponKind.SCYTHE) {
                    assertTrue(stats.reachModifier() > 0, kind + " " + tier);
                } else {
                    assertEquals(0.0, stats.reachModifier(), 1e-9, kind + " " + tier);
                }
            }
        }
    }

    @Test
    void miningOnlyKinds_doNotGetExtraSweepOrReach() {
        for (WeaponKind kind : new WeaponKind[] {WeaponKind.PICKAXE, WeaponKind.AXE}) {
            for (WeaponTier tier : WeaponTier.values()) {
                WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
                assertEquals(0.0, stats.sweepRatio(), 1e-9);
                assertEquals(0.0, stats.reachModifier(), 1e-9);
                assertTrue(stats.miningSpeed() > 0);
            }
        }
    }
}
