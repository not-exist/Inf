package com.inf.registry;

import com.inf.Inf;
import com.inf.menu.ForgeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Inf.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ForgeMenu>> FORGE_TABLE =
            MENUS.register("forge_table", () -> IMenuTypeExtension.create(ForgeMenu::new));

    private ModMenus() {
    }
}
