package com.inf.forge;

import com.inf.InfConfig;
import com.inf.block.ForgeBlockEntity;
import com.inf.item.BladeBlankItem;
import com.inf.item.HammerItem;
import com.inf.item.component.BladeData;
import com.inf.registry.ModDataComponents;
import com.inf.weapon.WeaponKind;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class ForgeService {
    private ForgeService() {
    }

    public static boolean strike(ForgeBlockEntity blockEntity, ServerPlayer player) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return false;
        }

        ItemStack blade = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        ItemStack hammer = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_HAMMER);
        if (!(blade.getItem() instanceof BladeBlankItem blank) || !HammerItem.canStrike(hammer)) {
            return false;
        }
        if (!ForgeTiming.canStrike(
                level.getGameTime(), blockEntity.lastStrikeGameTime(), InfConfig.strikeCooldownTicks())) {
            return false;
        }

        BladeData data = blade.get(ModDataComponents.BLADE_DATA.get());
        if (data == null) {
            data = new BladeData(blank.kind(), 0, 0, player.getRandom().nextLong());
        }

        boolean frenzy = blockEntity.frenzyStrikesLeft() > 0;
        StrikeOutcome outcome = ForgeMath.strike(
                data.value(),
                data.strikes(),
                player.getRandom().nextDouble(),
                player.getRandom().nextInt(100),
                frenzy,
                InfConfig.forgeBalance());

        if (outcome.broken()) {
            blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_BLADE, ItemStack.EMPTY);
            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.9F, 0.9F);
            level.sendParticles(
                    ParticleTypes.CRIT,
                    blockEntity.getBlockPos().getX() + 0.5,
                    blockEntity.getBlockPos().getY() + 0.9,
                    blockEntity.getBlockPos().getZ() + 0.5,
                    12,
                    0.2,
                    0.2,
                    0.2,
                    0.05);
        } else {
            blade.set(
                    ModDataComponents.BLADE_DATA.get(),
                    new BladeData(data.kind(), outcome.value(), outcome.strikes(), data.seed()));
            level.playSound(
                    null,
                    blockEntity.getBlockPos(),
                    SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS,
                    0.8F,
                    0.9F + player.getRandom().nextFloat() * 0.2F);
            level.sendParticles(
                    ParticleTypes.CRIT,
                    blockEntity.getBlockPos().getX() + 0.5,
                    blockEntity.getBlockPos().getY() + 0.9,
                    blockEntity.getBlockPos().getZ() + 0.5,
                    8,
                    0.2,
                    0.2,
                    0.2,
                    0.05);
        }

        int nextDamage = hammer.getDamageValue() + 1;
        if (nextDamage >= hammer.getMaxDamage()) {
            blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_HAMMER, ItemStack.EMPTY);
            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8F, 0.9F);
        } else {
            hammer.setDamageValue(nextDamage);
        }

        if (frenzy) {
            blockEntity.consumeFrenzyStrike();
        }
        blockEntity.markStrike(level.getGameTime());
        blockEntity.setChanged();
        return true;
    }

    public static boolean stop(ForgeBlockEntity blockEntity, ServerPlayer player) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack blade = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        BladeData data = blade.get(ModDataComponents.BLADE_DATA.get());
        if (data == null
                || !WeaponSettlement.canStop(
                        data.value(), blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_OUTPUT).isEmpty())) {
            return false;
        }

        ItemStack weapon = WeaponFactory.create(data.kind(), data.value(), data.strikes(), data.seed());
        blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_OUTPUT, weapon);
        blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_BLADE, ItemStack.EMPTY);
        level.playSound(null, blockEntity.getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.6F, 1.0F);
        blockEntity.setChanged();
        onWeaponCompleted(player, data.kind());
        return true;
    }

    public static void onWeaponCompleted(ServerPlayer player, WeaponKind kind) {
    }

    public static boolean activateFrenzy(ForgeBlockEntity blockEntity, ServerPlayer player) {
        return false;
    }
}
