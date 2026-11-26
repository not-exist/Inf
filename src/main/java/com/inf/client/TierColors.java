package com.inf.client;

import com.inf.weapon.WeaponTier;

public final class TierColors {
    private TierColors() {}

    public static int of(WeaponTier tier) {
        return switch (tier) {
            case CRUDE -> 0xFF8A8A8A;
            case COMMON -> 0xFFD8D8D8;
            case FINE -> 0xFF7FB4E8;
            case RARE -> 0xFFB07CFF;
            case EPIC -> 0xFFFFB33E;
            case LEGENDARY -> 0xFFFF4D5E;
        };
    }
}
