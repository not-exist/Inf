package com.inf.client;

import com.inf.Inf;
import com.inf.item.component.BladeData;
import com.inf.registry.ModDataComponents;
import com.inf.registry.ModItems;
import com.inf.registry.ModMenus;
import com.inf.weapon.WeaponTier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Inf.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.FORGE_TABLE.get(), ForgeScreen::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (ItemStack stack, int tintIndex) -> {
                    BladeData data = stack.get(ModDataComponents.BLADE_DATA.get());
                    return data == null ? -1 : TierColors.of(WeaponTier.forValue(data.value()));
                },
                ModItems.FORGED_SWORD.get(),
                ModItems.FORGED_PICKAXE.get(),
                ModItems.FORGED_AXE.get(),
                ModItems.FORGED_SCYTHE.get(),
                ModItems.FORGED_DAGGER.get());
    }
}
