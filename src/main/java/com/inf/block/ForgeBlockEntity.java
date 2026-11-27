package com.inf.block;

import com.inf.InfConfig;
import com.inf.menu.ForgeMenu;
import com.inf.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ForgeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BLADE = 0;
    public static final int SLOT_HAMMER = 1;
    public static final int SLOT_OUTPUT = 2;

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == SLOT_BLADE && getStackInSlot(slot).isEmpty()) {
                frenzyStrikesLeft = 0;
            }
            setChanged();
        }
    };

    private long lastStrikeGameTime = Long.MIN_VALUE;
    private int frenzyStrikesLeft;

    public ForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGE_TABLE.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public long lastStrikeGameTime() {
        return lastStrikeGameTime;
    }

    public void markStrike(long gameTime) {
        lastStrikeGameTime = gameTime;
    }

    public int frenzyStrikesLeft() {
        return frenzyStrikesLeft;
    }

    public void setFrenzyStrikesLeft(int value) {
        frenzyStrikesLeft = Math.max(0, value);
    }

    public void consumeFrenzyStrike() {
        if (frenzyStrikesLeft > 0) {
            frenzyStrikesLeft--;
        }
    }

    public void dropContents(ServerLevel level) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack);
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.inf.forge_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ForgeMenu(containerId, playerInventory, this, new ForgeData(this, player, InfConfig.forgeBalance()));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putLong("lastStrike", lastStrikeGameTime);
        tag.putInt("frenzyStrikes", frenzyStrikesLeft);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        lastStrikeGameTime = tag.contains("lastStrike") ? tag.getLong("lastStrike") : Long.MIN_VALUE;
        frenzyStrikesLeft = tag.getInt("frenzyStrikes");
    }
}
