package com.inf.block;

import com.inf.forge.ForgeBalance;
import com.inf.forge.ForgeMath;
import com.inf.item.component.BladeData;
import com.inf.registry.ModDataComponents;
import com.inf.weapon.WeaponTier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public final class ForgeData implements ContainerData {
    public static final int VALUE = 0;
    public static final int STRIKES = 1;
    public static final int RISK_PERMILLE = 2;
    public static final int TIER = 3;
    public static final int FRENZY_READY = 4;
    public static final int GAIN_MIN = 5;
    public static final int GAIN_MAX = 6;
    public static final int SIZE = 7;

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
        BladeData data = bladeData();
        int value = data == null ? 0 : data.value();
        int strikes = data == null ? 0 : data.strikes();
        return switch (index) {
            case VALUE -> value;
            case STRIKES -> strikes;
            case RISK_PERMILLE -> data == null
                    ? 0
                    : (int) Math.round(ForgeMath.breakChance(value, strikes, balance) * 1000.0);
            case TIER -> WeaponTier.forValue(value).ordinal();
            case FRENZY_READY -> 0;
            case GAIN_MIN -> data == null ? 0 : ForgeMath.valueGain(strikes, 0, balance);
            case GAIN_MAX -> data == null ? 0 : ForgeMath.valueGain(strikes, 99, balance);
            default -> 0;
        };
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
