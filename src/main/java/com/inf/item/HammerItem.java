package com.inf.item;

import com.inf.forge.HammerMath;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HammerItem extends Item {
    public static final int DURABILITY = 128;

    public HammerItem(Properties properties) {
        super(properties);
    }

    public static boolean canStrike(ItemStack stack) {
        return HammerMath.canStrike(stack.getMaxDamage(), stack.getDamageValue());
    }
}
