package com.inf.forge;

import com.inf.InfConfig;
import com.inf.advancement.ForgeAdvancements;
import com.inf.block.ForgeBlockEntity;
import com.inf.item.BladeBlankItem;
import com.inf.item.HammerItem;
import com.inf.item.component.BladeData;
import com.inf.registry.ModAttachments;
import com.inf.registry.ModDataComponents;
import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponTier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class ForgeService {
    private ForgeService() {}

    public static boolean strike(ForgeBlockEntity blockEntity, ServerPlayer player) {
        return strike(
                blockEntity,
                player,
                player.getRandom().nextDouble(),
                player.getRandom().nextInt(100));
    }

    /** Deterministic entry point used by tests; production uses randomized rolls. */
    public static boolean strike(ForgeBlockEntity blockEntity, ServerPlayer player, double outcomeRoll, int gainRoll) {
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
        StrikeOutcome outcome =
                ForgeMath.strike(data.value(), data.strikes(), outcomeRoll, gainRoll, frenzy, InfConfig.forgeBalance());

        switch (outcome.outcome()) {
            case BREAK -> {
                blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_BLADE, ItemStack.EMPTY);
                blockEntity.setFrenzyStrikesLeft(0);
                level.playSound(
                        null, blockEntity.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.9F, 0.9F);
                particles(level, blockEntity, ParticleTypes.CRIT, 14);
                ForgeAdvancements.award(player, ForgeAdvancements.BREAK_BLADE);
            }
            case DAMAGE -> {
                updateBlade(blade, data, outcome);
                level.playSound(
                        null, blockEntity.getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.7F, 0.55F);
                particles(level, blockEntity, ParticleTypes.SMOKE, 10);
                ForgeAdvancements.award(player, ForgeAdvancements.DAMAGE_BLADE);
            }
            case SUCCESS -> {
                updateBlade(blade, data, outcome);
                level.playSound(
                        null,
                        blockEntity.getBlockPos(),
                        SoundEvents.ANVIL_USE,
                        SoundSource.BLOCKS,
                        0.8F,
                        0.9F + player.getRandom().nextFloat() * 0.2F);
                particles(level, blockEntity, ParticleTypes.CRIT, 8);
            }
            case GREAT_SUCCESS -> {
                updateBlade(blade, data, outcome);
                level.playSound(
                        null, blockEntity.getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.9F, 1.55F);
                level.playSound(
                        null, blockEntity.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.35F, 1.6F);
                particles(level, blockEntity, ParticleTypes.ENCHANT, 18);
                ForgeAdvancements.award(player, ForgeAdvancements.GREAT_SUCCESS);
            }
        }
        ForgeAdvancements.award(player, ForgeAdvancements.FIRST_STRIKE);

        int nextDamage = hammer.getDamageValue() + 1;
        if (nextDamage >= hammer.getMaxDamage()) {
            blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_HAMMER, ItemStack.EMPTY);
            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8F, 0.9F);
            ForgeAdvancements.award(player, ForgeAdvancements.HAMMER_BREAK);
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

    private static void updateBlade(ItemStack blade, BladeData data, StrikeOutcome outcome) {
        blade.set(
                ModDataComponents.BLADE_DATA.get(),
                new BladeData(data.kind(), outcome.value(), outcome.strikes(), data.seed()));
    }

    private static void particles(
            ServerLevel level,
            ForgeBlockEntity blockEntity,
            net.minecraft.core.particles.ParticleOptions type,
            int count) {
        level.sendParticles(
                type,
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.9,
                blockEntity.getBlockPos().getZ() + 0.5,
                count,
                0.25,
                0.25,
                0.25,
                0.05);
    }

    public static boolean stop(ForgeBlockEntity blockEntity, ServerPlayer player) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack blade = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        BladeData data = blade.get(ModDataComponents.BLADE_DATA.get());
        if (data == null
                || !WeaponSettlement.canStop(
                        data.value(),
                        blockEntity
                                .inventory()
                                .getStackInSlot(ForgeBlockEntity.SLOT_OUTPUT)
                                .isEmpty())) {
            return false;
        }

        ItemStack weapon = WeaponFactory.create(data.kind(), data.value(), data.strikes(), data.seed());
        blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_OUTPUT, weapon);
        blockEntity.inventory().setStackInSlot(ForgeBlockEntity.SLOT_BLADE, ItemStack.EMPTY);
        blockEntity.setFrenzyStrikesLeft(0);
        level.playSound(null, blockEntity.getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.6F, 1.0F);
        blockEntity.setChanged();
        onWeaponCompleted(player, data.kind(), data.value());
        return true;
    }

    public static void onWeaponCompleted(ServerPlayer player, WeaponKind kind, int value) {
        player.setData(
                ModAttachments.FRENZY,
                player.getData(ModAttachments.FRENZY).onWeaponCompleted(kind, InfConfig.frenzyRequired()));

        ForgeAdvancements.award(player, ForgeAdvancements.FIRST_WEAPON);
        ForgeAdvancements.award(player, ForgeAdvancements.kind(kind));
        WeaponTier tier = WeaponTier.forValue(value);
        switch (tier) {
            case COMMON -> ForgeAdvancements.award(player, ForgeAdvancements.tier(WeaponTier.COMMON));
            case FINE -> ForgeAdvancements.award(player, ForgeAdvancements.tier(WeaponTier.FINE));
            case RARE -> ForgeAdvancements.award(player, ForgeAdvancements.tier(WeaponTier.RARE));
            case EPIC -> ForgeAdvancements.award(player, ForgeAdvancements.tier(WeaponTier.EPIC));
            case LEGENDARY -> ForgeAdvancements.award(player, ForgeAdvancements.tier(WeaponTier.LEGENDARY));
            case CRUDE -> {}
        }
        ForgeAdvancements.awardAllKindsIfDone(player);
    }

    public static boolean activateFrenzy(ForgeBlockEntity blockEntity, ServerPlayer player) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack blade = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        BladeData data = blade.get(ModDataComponents.BLADE_DATA.get());
        FrenzyState state = player.getData(ModAttachments.FRENZY);
        if (data == null || data.value() <= 0 || !state.ready()) {
            return false;
        }
        blockEntity.setFrenzyStrikesLeft(InfConfig.frenzyFreeStrikes());
        player.setData(ModAttachments.FRENZY, state.activated());
        level.playSound(null, blockEntity.getBlockPos(), SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.5F, 1.2F);
        blockEntity.setChanged();
        ForgeAdvancements.award(player, ForgeAdvancements.FRENZY);
        return true;
    }
}
