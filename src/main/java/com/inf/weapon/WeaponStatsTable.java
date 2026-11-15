package com.inf.weapon;

public final class WeaponStatsTable {
    private static final double[][] DAMAGE = {
            {4, 5, 6, 7, 9, 11},        // sword
            {2, 3, 4, 5, 6, 8},         // pickaxe
            {6, 7, 8, 10, 12, 15},      // axe
            {5, 6, 7, 9, 11, 13},       // scythe
            {3, 4, 5, 6, 8, 10}         // dagger
    };
    private static final double[][] SPEED = {
            {-2.4, -2.4, -2.3, -2.2, -2.1, -2.0},
            {-2.8, -2.7, -2.6, -2.5, -2.4, -2.3},
            {-3.0, -3.0, -2.9, -2.8, -2.7, -2.6},
            {-2.6, -2.6, -2.5, -2.4, -2.3, -2.2},
            {-1.5, -1.4, -1.3, -1.2, -1.1, -1.0}
    };
    private static final double[][] REACH = {
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0},
            {0.50, 0.50, 0.75, 1.00, 1.25, 1.50},
            {-1.00, -0.90, -0.80, -0.70, -0.60, -0.50}
    };
    private static final double[][] SWEEP = {
            {0.50, 0.55, 0.60, 0.65, 0.70, 0.80},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0},
            {0.80, 0.85, 0.90, 0.95, 1.00, 1.20},
            {0, 0, 0, 0, 0, 0}
    };
    private static final double[][] MINING = {
            {0, 0, 0, 0, 0, 0},
            {4, 6, 8, 10, 12, 15},
            {4, 6, 8, 10, 12, 15},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0}
    };
    private static final int[][] HARVEST = {
            {0, 0, 0, 0, 0, 0},
            {1, 2, 2, 3, 3, 4},
            {1, 2, 2, 3, 3, 4},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0}
    };

    private WeaponStatsTable() {
    }

    public static WeaponStats forTier(WeaponKind kind, WeaponTier tier) {
        int kindIndex = kind.ordinal();
        int tierIndex = tier.ordinal();
        return new WeaponStats(
                DAMAGE[kindIndex][tierIndex],
                SPEED[kindIndex][tierIndex],
                REACH[kindIndex][tierIndex],
                SWEEP[kindIndex][tierIndex],
                MINING[kindIndex][tierIndex],
                HARVEST[kindIndex][tierIndex]);
    }
}
