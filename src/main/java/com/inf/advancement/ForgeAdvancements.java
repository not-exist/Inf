package com.inf.advancement;

import com.inf.Inf;
import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponTier;
import java.util.List;
import java.util.Locale;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ForgeAdvancements {
    public static final String ROOT = "root";
    public static final String FIRST_STRIKE = "first_strike";
    public static final String FIRST_WEAPON = "first_weapon";
    public static final String BREAK_BLADE = "break_blade";
    public static final String DAMAGE_BLADE = "damage_blade";
    public static final String GREAT_SUCCESS = "great_success";
    public static final String HAMMER_BREAK = "hammer_break";
    public static final String FRENZY = "frenzy";
    public static final String ALL_KINDS = "all_kinds";

    private ForgeAdvancements() {
    }

    public static String kind(WeaponKind kind) {
        return "kind_" + kind.id;
    }

    public static String tier(WeaponTier tier) {
        return "tier_" + tier.name().toLowerCase(Locale.ROOT);
    }

    /** Every static node; {@code kind_*} and {@code tier_*} are generated from the enums. */
    public static List<String> staticIds() {
        return List.of(
                ROOT, FIRST_STRIKE, FIRST_WEAPON, BREAK_BLADE, DAMAGE_BLADE,
                GREAT_SUCCESS, HAMMER_BREAK, FRENZY, ALL_KINDS);
    }

    public static List<String> allIds() {
        List<String> ids = new java.util.ArrayList<>(staticIds());
        for (WeaponKind kind : WeaponKind.values()) {
            ids.add(kind(kind));
        }
        for (WeaponTier tier : WeaponTier.values()) {
            if (tier != WeaponTier.CRUDE) {
                ids.add(tier(tier));
            }
        }
        return List.copyOf(ids);
    }

    public static void award(ServerPlayer player, String path) {
        if (player.getServer() == null) {
            return;
        }
        AdvancementHolder holder = player.getServer()
                .getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Inf.MODID, path));
        if (holder != null) {
            player.getAdvancements().award(holder, "unlocked");
        }
    }

    public static boolean done(ServerPlayer player, String path) {
        if (player.getServer() == null) {
            return false;
        }
        AdvancementHolder holder = player.getServer()
                .getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Inf.MODID, path));
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    public static void awardAllKindsIfDone(ServerPlayer player) {
        for (WeaponKind kind : WeaponKind.values()) {
            if (!done(player, kind(kind))) {
                return;
            }
        }
        award(player, ALL_KINDS);
    }
}
