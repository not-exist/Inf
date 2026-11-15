package com.inf;

import com.inf.registry.ModBlockEntities;
import com.inf.registry.ModBlocks;
import com.inf.registry.ModCreativeTabs;
import com.inf.registry.ModDataComponents;
import com.inf.registry.ModItems;
import com.inf.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Inf.MODID)
public final class Inf {
    public static final String MODID = "inf";

    public Inf(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, InfConfig.SPEC);
    }
}
