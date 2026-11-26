package com.inf.forge;

public final class WeaponSettlement {
    private WeaponSettlement() {}

    public static boolean canStop(int bladeValue, boolean outputEmpty) {
        return bladeValue > 0 && outputEmpty;
    }
}
