package com.inf.forge;

public record ForgeBalance(
        double baseRisk,
        double riskPerStrike,
        double riskPerValue,
        double maxRisk,
        int baseGain,
        int gainPerStrike,
        int gainVariance,
        int minGain) {

    public static final ForgeBalance DEFAULT =
            new ForgeBalance(0.015, 0.0012, 0.00003, 0.85, 12, 1, 8, 4);

    public static ForgeBalance of(
            double baseRisk,
            double riskPerStrike,
            double riskPerValue,
            double maxRisk,
            int baseGain,
            int gainPerStrike,
            int gainVariance,
            int minGain) {
        return new ForgeBalance(
                baseRisk, riskPerStrike, riskPerValue, maxRisk, baseGain, gainPerStrike, gainVariance, minGain);
    }
}
