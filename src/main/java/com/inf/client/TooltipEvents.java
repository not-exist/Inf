package com.inf.client;

import com.inf.Inf;
import com.inf.item.component.BladeData;
import com.inf.registry.ModDataComponents;
import com.inf.weapon.WeaponTier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.network.chat.Component;

@EventBusSubscriber(modid = Inf.MODID, value = Dist.CLIENT)
public final class TooltipEvents {
    private TooltipEvents() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        BladeData data = event.getItemStack().get(ModDataComponents.BLADE_DATA.get());
        if (data == null) {
            return;
        }
        WeaponTier tier = WeaponTier.forValue(data.value());
        event.getToolTip().add(Component.translatable("inf.tooltip.value", data.value(), data.strikes()));
        event.getToolTip()
                .add(Component.translatable("inf.tooltip.tier", Component.translatable(tier.translationKey())));
    }
}
