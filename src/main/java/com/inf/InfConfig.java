package com.inf;

import com.inf.forge.ForgeBalance;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class InfConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue BASE_RISK =
            BUILDER.comment("Base shatter chance of the first strike.").defineInRange("baseRisk", 0.015, 0.0, 1.0);
    private static final ModConfigSpec.DoubleValue RISK_PER_STRIKE =
            BUILDER.defineInRange("riskPerStrike", 0.0015, 0.0, 1.0);
    private static final ModConfigSpec.DoubleValue RISK_PER_VALUE =
            BUILDER.defineInRange("riskPerValue", 0.000035, 0.0, 1.0);
    private static final ModConfigSpec.DoubleValue MAX_RISK =
            BUILDER.defineInRange("maxRisk", 0.85, 0.0, 1.0);
    private static final ModConfigSpec.IntValue BASE_GAIN = BUILDER.defineInRange("baseGain", 12, 0, 1000);
    private static final ModConfigSpec.IntValue GAIN_PER_STRIKE = BUILDER.defineInRange("gainPerStrike", 1, 0, 1000);
    private static final ModConfigSpec.IntValue GAIN_VARIANCE = BUILDER.defineInRange("gainVariance", 8, 0, 1000);
    private static final ModConfigSpec.IntValue MIN_GAIN = BUILDER.defineInRange("minGain", 4, 0, 1000);

    private static final ModConfigSpec.IntValue FRENZY_REQUIRED = BUILDER.defineInRange("frenzyRequired", 10, 1, 1000);
    private static final ModConfigSpec.IntValue FRENZY_FREE_STRIKES =
            BUILDER.defineInRange("frenzyFreeStrikes", 3, 1, 100);
    private static final ModConfigSpec.IntValue STRIKE_COOLDOWN_TICKS =
            BUILDER.defineInRange("strikeCooldownTicks", 4, 0, 200);
    private static final ModConfigSpec.BooleanValue UI_ANIMATIONS = BUILDER.define("uiAnimations", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private InfConfig() {
    }

    public static ForgeBalance forgeBalance() {
        return ForgeBalance.of(
                BASE_RISK.get(),
                RISK_PER_STRIKE.get(),
                RISK_PER_VALUE.get(),
                MAX_RISK.get(),
                BASE_GAIN.get(),
                GAIN_PER_STRIKE.get(),
                GAIN_VARIANCE.get(),
                MIN_GAIN.get());
    }

    public static int frenzyRequired() {
        return FRENZY_REQUIRED.get();
    }

    public static int frenzyFreeStrikes() {
        return FRENZY_FREE_STRIKES.get();
    }

    public static int strikeCooldownTicks() {
        return STRIKE_COOLDOWN_TICKS.get();
    }

    public static boolean uiAnimations() {
        return UI_ANIMATIONS.get();
    }
}
