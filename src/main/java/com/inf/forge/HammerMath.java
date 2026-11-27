package com.inf.forge;

public final class HammerMath {
    private HammerMath() {}

    public static int remainingAfterStrikes(int maxDurability, int strikes) {
        return Math.max(0, maxDurability - strikes);
    }

    public static boolean canStrike(int maxDurability, int damageValue) {
        return maxDurability - damageValue > 0;
    }
}
