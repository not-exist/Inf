package com.inf.registry;

import com.inf.Inf;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Inf.MODID);

    public static final Supplier<CreativeModeTab> INF = TABS.register(
            "inf",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.inf"))
                    .icon(() -> new ItemStack(ModItems.FORGED_SWORD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HAMMER.get());
                        output.accept(ModBlocks.FORGE_TABLE_ITEM.get());
                        output.accept(ModItems.SWORD_BLANK.get());
                        output.accept(ModItems.PICKAXE_BLANK.get());
                        output.accept(ModItems.AXE_BLANK.get());
                        output.accept(ModItems.SCYTHE_BLANK.get());
                        output.accept(ModItems.DAGGER_BLANK.get());
                        output.accept(ModItems.FORGED_SWORD.get());
                        output.accept(ModItems.FORGED_PICKAXE.get());
                        output.accept(ModItems.FORGED_AXE.get());
                        output.accept(ModItems.FORGED_SCYTHE.get());
                        output.accept(ModItems.FORGED_DAGGER.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
