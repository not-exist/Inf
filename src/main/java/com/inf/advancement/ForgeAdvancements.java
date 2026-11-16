package com.inf.advancement;

import com.inf.Inf;
import com.inf.weapon.WeaponKind;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ForgeAdvancements {
    private ForgeAdvancements() {
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
            if (!done(player, "kind_" + kind.id)) {
                return;
            }
        }
        award(player, "all_kinds");
    }
}
