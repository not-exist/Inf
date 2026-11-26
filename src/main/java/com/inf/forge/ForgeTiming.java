package com.inf.forge;

public final class ForgeTiming {
    private ForgeTiming() {}

    public static boolean canStrike(long gameTime, long lastStrikeGameTime, int cooldownTicks) {
        if (cooldownTicks <= 0 || lastStrikeGameTime == Long.MIN_VALUE) {
            return true;
        }
        return gameTime - lastStrikeGameTime >= cooldownTicks;
    }
}
