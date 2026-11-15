package com.inf.forge;

public final class ForgeMath {
    private ForgeMath() {
    }

    public static double breakChance(int value, int strikes, ForgeBalance balance) {
        return Math.min(
                balance.maxRisk(),
                balance.baseRisk() + balance.riskPerStrike() * strikes + balance.riskPerValue() * value);
    }

    public static int valueGain(int strikes, int gainRoll0to99, ForgeBalance balance) {
        int roll = Math.max(0, Math.min(99, gainRoll0to99));
        int progress = Math.min(strikes / 2, 10);
        return Math.max(
                balance.minGain(),
                balance.baseGain() + progress + roll * balance.gainVariance() / 100);
    }

    public static StrikeOutcome strike(
            int value,
            int strikes,
            double breakRoll,
            int gainRoll,
            boolean frenzy,
            ForgeBalance balance) {
        double risk = breakChance(value, strikes, balance);
        int gain = valueGain(strikes, gainRoll, balance);
        if (!frenzy && breakRoll < risk) {
            return new StrikeOutcome(value, strikes, true, 0, risk);
        }
        return new StrikeOutcome(value + gain, strikes + 1, false, gain, risk);
    }
}
