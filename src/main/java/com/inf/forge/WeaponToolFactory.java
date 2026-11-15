package com.inf.forge;

import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponStats;
import com.inf.weapon.WeaponStatsTable;
import com.inf.weapon.WeaponTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;

public final class WeaponToolFactory {
    private WeaponToolFactory() {
    }

    public static Tool build(WeaponKind kind, WeaponTier tier) {
        WeaponStats stats = WeaponStatsTable.forTier(kind, tier);
        List<Tool.Rule> rules = new ArrayList<>();
        if (stats.harvestLevel() < 2) {
            rules.add(Tool.Rule.deniesDrops(BlockTags.NEEDS_IRON_TOOL));
        }
        if (stats.harvestLevel() < 3) {
            rules.add(Tool.Rule.deniesDrops(BlockTags.NEEDS_DIAMOND_TOOL));
        }
        TagKey<Block> mineable =
                kind == WeaponKind.AXE ? BlockTags.MINEABLE_WITH_AXE : BlockTags.MINEABLE_WITH_PICKAXE;
        rules.add(Tool.Rule.minesAndDrops(mineable, (float) stats.miningSpeed()));
        return new Tool(rules, 1.0F, 1);
    }
}
