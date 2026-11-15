package com.inf.menu;

public final class ForgeMenuRules {
    private ForgeMenuRules() {
    }

    public static boolean stillValid(boolean sameBlockEntity, double distanceSqr) {
        return sameBlockEntity && distanceSqr <= 64.0;
    }
}
