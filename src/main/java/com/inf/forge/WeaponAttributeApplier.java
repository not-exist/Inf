package com.inf.forge;

import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponStats;
import com.inf.weapon.WeaponStatsTable;
import com.inf.weapon.WeaponTier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class WeaponAttributeApplier {
    private WeaponAttributeApplier() {
    }

    public static void apply(ItemStack stack, WeaponKind kind, WeaponTier tier) {
        WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        add(builder, Attributes.ATTACK_DAMAGE, kind, "damage", stats.attackDamage());
        add(builder, Attributes.ATTACK_SPEED, kind, "speed", stats.attackSpeedModifier());

        switch (kind) {
            case SWORD -> add(builder, Attributes.SWEEPING_DAMAGE_RATIO, kind, "sweep", stats.sweepRatio());
            case SCYTHE -> {
                add(builder, Attributes.SWEEPING_DAMAGE_RATIO, kind, "sweep", stats.sweepRatio());
                add(builder, Attributes.ENTITY_INTERACTION_RANGE, kind, "reach", stats.reachModifier());
            }
            case DAGGER -> add(builder, Attributes.ENTITY_INTERACTION_RANGE, kind, "reach", stats.reachModifier());
            case PICKAXE, AXE -> {
            }
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void add(
            ItemAttributeModifiers.Builder builder,
            Holder<Attribute> attribute,
            WeaponKind kind,
            String name,
            double amount) {
        builder.add(
                attribute,
                new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath("inf", "forged/" + kind.id + "/" + name),
                        amount,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
}
