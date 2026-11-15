package com.inf.item.forged;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tiers;

public class ForgedPickaxeItem extends DiggerItem {
    public ForgedPickaxeItem(Properties properties) {
        super(Tiers.IRON, BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }
}
