package com.inf.registry;

import com.inf.Inf;
import com.inf.block.ForgeBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Inf.MODID);

    public static final DeferredBlock<ForgeBlock> FORGE_TABLE = BLOCKS.register(
            "forge_table",
            () -> new ForgeBlock(BlockBehaviour.Properties.of()
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANVIL)));

    public static final DeferredItem<BlockItem> FORGE_TABLE_ITEM =
            ModItems.ITEMS.registerSimpleBlockItem("forge_table", FORGE_TABLE);

    private ModBlocks() {
    }
}
