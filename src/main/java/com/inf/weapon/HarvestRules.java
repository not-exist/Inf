package com.inf.weapon;

public final class HarvestRules {
    private HarvestRules() {}

    public static int requiredLevel(boolean needsStone, boolean needsIron, boolean needsDiamond) {
        if (needsDiamond) {
            return 4;
        }
        if (needsIron) {
            return 2;
        }
        if (needsStone) {
            return 1;
        }
        return 0;
    }

    public static boolean canHarvest(int harvestLevel, boolean requiresCorrectTool, int requiredLevel) {
        return !requiresCorrectTool || harvestLevel >= requiredLevel;
    }

    public static boolean deniesIronDrops(int harvestLevel) {
        return harvestLevel < 2;
    }

    public static boolean deniesDiamondDrops(int harvestLevel) {
        return harvestLevel < 4;
    }
}
