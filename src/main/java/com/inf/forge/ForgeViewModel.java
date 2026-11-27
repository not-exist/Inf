package com.inf.forge;

import com.inf.item.component.BladeData;
import com.inf.weapon.WeaponTier;

/**
 * Pure mapping of the forge state to the {@code ContainerData} contract that the
 * client menu reads. Kept free of Minecraft types so the index mapping, rounding
 * and tier ordinals are unit-testable.
 */
public final class ForgeViewModel {
    public static final int VALUE = 0;
    public static final int STRIKES = 1;
    public static final int RISK_PERMILLE = 2;
    public static final int TIER = 3;
    public static final int FRENZY_READY = 4;
    public static final int GAIN_MIN = 5;
    public static final int GAIN_MAX = 6;
    public static final int FRENZY_PROGRESS = 7;
    public static final int FRENZY_ACTIVE = 8;
    public static final int FRENZY_REQUIRED = 9;
    public static final int BREAK_PERMILLE = 10;
    public static final int DAMAGE_PERMILLE = 11;
    public static final int SUCCESS_PERMILLE = 12;
    public static final int GREAT_PERMILLE = 13;
    public static final int SIZE = 14;

    private final int[] values;

    private ForgeViewModel(int[] values) {
        this.values = values;
    }

    public static ForgeViewModel of(
            BladeData blade,
            boolean frenzyReady,
            int frenzyProgress,
            int frenzyActive,
            int frenzyRequired,
            ForgeBalance balance) {
        int value = blade == null ? 0 : blade.value();
        int strikes = blade == null ? 0 : blade.strikes();
        ForgeRates rates =
                blade == null ? new ForgeRates(0, 0, 0, 0) : ForgeRates.of(value, strikes, frenzyActive > 0, balance);

        int[] data = new int[SIZE];
        data[VALUE] = value;
        data[STRIKES] = strikes;
        data[RISK_PERMILLE] = permille(rates.breakChance());
        data[TIER] = WeaponTier.forValue(value).ordinal();
        data[FRENZY_READY] = frenzyReady ? 1 : 0;
        data[GAIN_MIN] = blade == null ? 0 : ForgeMath.valueGain(strikes, 0, balance);
        data[GAIN_MAX] = blade == null ? 0 : ForgeMath.valueGain(strikes, 99, balance);
        data[FRENZY_PROGRESS] = blade == null ? 0 : frenzyProgress;
        data[FRENZY_ACTIVE] = frenzyActive;
        data[FRENZY_REQUIRED] = frenzyRequired;
        data[BREAK_PERMILLE] = permille(rates.breakChance());
        data[DAMAGE_PERMILLE] = permille(rates.damageChance());
        data[SUCCESS_PERMILLE] = permille(rates.successChance());
        data[GREAT_PERMILLE] = permille(rates.greatChance());
        return new ForgeViewModel(data);
    }

    public int get(int index) {
        return index >= 0 && index < SIZE ? values[index] : 0;
    }

    public static int permille(double chance) {
        return (int) Math.round(chance * 1000.0);
    }
}
