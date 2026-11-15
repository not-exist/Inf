package com.inf.combat;

import com.inf.Inf;
import com.inf.item.forged.ForgedDaggerItem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@EventBusSubscriber(modid = Inf.MODID)
public final class CombatEvents {
    private CombatEvents() {
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity().getLastHurtByMob() instanceof Player player)) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof ForgedDaggerItem) {
            event.setStrength(0.0F);
        }
    }
}
