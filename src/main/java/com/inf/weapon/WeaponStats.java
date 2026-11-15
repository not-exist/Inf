package com.inf.weapon;

public record WeaponStats(
        double attackDamage,
        double attackSpeedModifier,
        double reachModifier,
        double sweepRatio,
        double miningSpeed,
        int harvestLevel) {
}
