package com.inf.tools;

public record BalanceReport(
        double crudeOrBetter,
        double commonOrBetter,
        double fineOrBetter,
        double rareOrBetter,
        double epicOrBetter,
        double legendary,
        double averageStrikes) {
}
