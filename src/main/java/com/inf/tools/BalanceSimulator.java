package com.inf.tools;

import com.inf.forge.ForgeBalance;
import com.inf.forge.ForgeMath;
import com.inf.forge.StrikeOutcome;
import com.inf.weapon.WeaponTier;
import java.util.Random;

public final class BalanceSimulator {
    private static final int LEGENDARY_VALUE = 1000;
    private static final int MAX_STRIKES = 10_000;

    private BalanceSimulator() {
    }

    public static BalanceReport run(int trials, long seed, ForgeBalance balance) {
        if (trials <= 0) {
            throw new IllegalArgumentException("trials must be positive");
        }
        Random random = new Random(seed);
        int[] reached = new int[WeaponTier.values().length];
        long totalStrikes = 0;

        for (int trial = 0; trial < trials; trial++) {
            int value = 0;
            int strikes = 0;
            while (value < LEGENDARY_VALUE && strikes < MAX_STRIKES) {
                StrikeOutcome outcome =
                        ForgeMath.strike(value, strikes, random.nextDouble(), random.nextInt(100), false, balance);
                strikes++;
                if (outcome.broken()) {
                    break;
                }
                value = outcome.value();
            }
            totalStrikes += strikes;
            int tier = WeaponTier.forValue(value).ordinal();
            for (int index = 0; index <= tier; index++) {
                reached[index]++;
            }
        }

        return new BalanceReport(
                reached[0] / (double) trials,
                reached[1] / (double) trials,
                reached[2] / (double) trials,
                reached[3] / (double) trials,
                reached[4] / (double) trials,
                reached[5] / (double) trials,
                totalStrikes / (double) trials);
    }
}
