package com.inf.forge;

import com.inf.item.component.BladeData;
import com.inf.registry.ModDataComponents;
import com.inf.registry.ModItems;
import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class WeaponFactory {
    private WeaponFactory() {
    }

    public static ItemStack create(WeaponKind kind, int value, int strikes, long seed) {
        WeaponTier tier = WeaponTier.forValue(value);
        ItemStack stack = new ItemStack(ModItems.forged(kind));
        stack.set(ModDataComponents.BLADE_DATA.get(), new BladeData(kind, value, strikes, seed));
        stack.set(
                DataComponents.ITEM_NAME,
                Component.translatable(
                        "inf.weapon.name",
                        Component.translatable(tier.translationKey()),
                        Component.translatable(kind.translationKey())));
        stack.set(DataComponents.RARITY, rarity(tier));
        WeaponAttributeApplier.apply(stack, kind, tier);
        if (kind == WeaponKind.PICKAXE || kind == WeaponKind.AXE) {
            stack.set(DataComponents.TOOL, WeaponToolFactory.build(kind, tier));
        }
        return stack;
    }

    private static Rarity rarity(WeaponTier tier) {
        return switch (tier) {
            case CRUDE, COMMON -> Rarity.COMMON;
            case FINE -> Rarity.UNCOMMON;
            case RARE -> Rarity.RARE;
            case EPIC, LEGENDARY -> Rarity.EPIC;
        };
    }
}
