package com.inf.block;

import com.inf.InfConfig;
import com.inf.forge.ForgeBalance;
import com.inf.forge.ForgeViewModel;
import com.inf.item.component.BladeData;
import com.inf.registry.ModAttachments;
import com.inf.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public final class ForgeData implements ContainerData {
    public static final int VALUE = ForgeViewModel.VALUE;
    public static final int STRIKES = ForgeViewModel.STRIKES;
    public static final int RISK_PERMILLE = ForgeViewModel.RISK_PERMILLE;
    public static final int TIER = ForgeViewModel.TIER;
    public static final int FRENZY_READY = ForgeViewModel.FRENZY_READY;
    public static final int GAIN_MIN = ForgeViewModel.GAIN_MIN;
    public static final int GAIN_MAX = ForgeViewModel.GAIN_MAX;
    public static final int FRENZY_PROGRESS = ForgeViewModel.FRENZY_PROGRESS;
    public static final int FRENZY_ACTIVE = ForgeViewModel.FRENZY_ACTIVE;
    public static final int FRENZY_REQUIRED = ForgeViewModel.FRENZY_REQUIRED;
    public static final int BREAK_PERMILLE = ForgeViewModel.BREAK_PERMILLE;
    public static final int DAMAGE_PERMILLE = ForgeViewModel.DAMAGE_PERMILLE;
    public static final int SUCCESS_PERMILLE = ForgeViewModel.SUCCESS_PERMILLE;
    public static final int GREAT_PERMILLE = ForgeViewModel.GREAT_PERMILLE;
    public static final int SIZE = ForgeViewModel.SIZE;

    private final ForgeBlockEntity blockEntity;
    private final Player player;
    private final ForgeBalance balance;

    public ForgeData(ForgeBlockEntity blockEntity, Player player, ForgeBalance balance) {
        this.blockEntity = blockEntity;
        this.player = player;
        this.balance = balance;
    }

    @Override
    public int get(int index) {
        return view().get(index);
    }

    private ForgeViewModel view() {
        BladeData data = bladeData();
        int progress = data == null ? 0 : player.getData(ModAttachments.FRENZY).progress(data.kind());
        return ForgeViewModel.of(
                data,
                player.getData(ModAttachments.FRENZY).ready(),
                progress,
                blockEntity.frenzyStrikesLeft(),
                InfConfig.frenzyRequired(),
                balance);
    }

    public BladeData bladeData() {
        ItemStack stack = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        return stack.isEmpty() ? null : stack.get(ModDataComponents.BLADE_DATA.get());
    }

    public Player player() {
        return player;
    }

    @Override
    public void set(int index, int value) {
        // Read-only view of the block entity and the player's frenzy state.
    }

    @Override
    public int getCount() {
        return SIZE;
    }
}
