package com.inf.tools;

import com.inf.forge.ForgeBalance;

public final class BalanceSim {
    private BalanceSim() {
    }

    public static void main(String[] args) {
        int trials = 100_000;
        long seed = 42L;
        BalanceReport report = BalanceSimulator.run(trials, seed, ForgeBalance.DEFAULT);

        System.out.printf("ForgeBalance DEFAULT, trials=%d, seed=%d%n", trials, seed);
        System.out.printf("crude or better:     %6.2f%%%n", report.crudeOrBetter() * 100.0);
        System.out.printf("common or better:    %6.2f%%%n", report.commonOrBetter() * 100.0);
        System.out.printf("fine or better:      %6.2f%%%n", report.fineOrBetter() * 100.0);
        System.out.printf("rare or better:      %6.2f%%%n", report.rareOrBetter() * 100.0);
        System.out.printf("epic or better:      %6.2f%%%n", report.epicOrBetter() * 100.0);
        System.out.printf("legendary:           %6.2f%%%n", report.legendary() * 100.0);
        System.out.printf("average strikes:     %6.2f%n", report.averageStrikes());
    }
}
