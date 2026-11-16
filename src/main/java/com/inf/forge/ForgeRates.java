package com.inf.forge;

public record ForgeRates(double breakChance, double damageChance, double successChance, double greatChance) {
    public static ForgeRates of(int value, int strikes, boolean frenzy, ForgeBalance balance) {
        if (frenzy) {
            return new ForgeRates(0.0, 0.0, 0.75, 0.25);
        }
        double breakChance = ForgeMath.breakChance(value, strikes, balance);
        double damageChance = (1.0 - breakChance) * 0.08;
        double greatChance = (1.0 - breakChance - damageChance) * 0.15;
        double successChance = 1.0 - breakChance - damageChance - greatChance;
        return new ForgeRates(breakChance, damageChance, successChance, greatChance);
    }
}
