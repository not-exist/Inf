package com.inf.forge;

public record StrikeOutcome(int value, int strikes, ForgeOutcome outcome, int gain, int loss, ForgeRates rates) {
    public boolean broken() {
        return outcome == ForgeOutcome.BREAK;
    }

    public double breakChance() {
        return rates.breakChance();
    }
}
