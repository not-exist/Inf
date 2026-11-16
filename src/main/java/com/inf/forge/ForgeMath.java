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
        int progress = Math.min(strikes / 2 * balance.gainPerStrike(), 10);
        return Math.max(
                balance.minGain(),
                balance.baseGain() + progress + roll * balance.gainVariance() / 100);
    }

    public static StrikeOutcome strike(
            int value,
            int strikes,
            double outcomeRoll,
            int gainRoll,
            boolean frenzy,
            ForgeBalance balance) {
        ForgeRates rates = ForgeRates.of(value, strikes, frenzy, balance);
        ForgeOutcome outcome;
        if (outcomeRoll < rates.breakChance()) {
            outcome = ForgeOutcome.BREAK;
        } else if (outcomeRoll < rates.breakChance() + rates.damageChance()) {
            outcome = ForgeOutcome.DAMAGE;
        } else if (outcomeRoll < rates.breakChance() + rates.damageChance() + rates.greatChance()) {
            outcome = ForgeOutcome.GREAT_SUCCESS;
        } else {
            outcome = ForgeOutcome.SUCCESS;
        }

        int gain = valueGain(strikes, gainRoll, balance);
        return switch (outcome) {
            case BREAK -> new StrikeOutcome(value, strikes, outcome, 0, 0, rates);
            case DAMAGE -> {
                int loss = Math.min(value, Math.max(1, value / 10));
                yield new StrikeOutcome(value - loss, strikes + 1, outcome, 0, loss, rates);
            }
            case SUCCESS -> new StrikeOutcome(value + gain, strikes + 1, outcome, gain, 0, rates);
            case GREAT_SUCCESS -> new StrikeOutcome(value + gain * 2, strikes + 1, outcome, gain * 2, 0, rates);
        };
    }
}
