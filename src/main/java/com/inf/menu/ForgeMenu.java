package com.inf.menu;

import com.inf.block.ForgeBlockEntity;
import com.inf.block.ForgeData;
import com.inf.item.BladeBlankItem;
import com.inf.item.HammerItem;
import com.inf.item.component.BladeData;
import com.inf.registry.ModDataComponents;
import com.inf.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ForgeMenu extends AbstractContainerMenu {
    public static final int SLOT_BLADE = 0;
    public static final int SLOT_HAMMER = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final int ACTION_STRIKE = 0;
    public static final int ACTION_STOP = 1;
    public static final int ACTION_FRENZY = 2;

    private final ForgeBlockEntity blockEntity;
    private final ContainerData data;

    public ForgeMenu(int containerId, Inventory playerInventory, ForgeBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.FORGE_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        addSlot(new SlotItemHandler(blockEntity.inventory(), ForgeBlockEntity.SLOT_BLADE, 16, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BladeBlankItem;
            }
        });
        addSlot(new SlotItemHandler(blockEntity.inventory(), ForgeBlockEntity.SLOT_HAMMER, 16, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof HammerItem;
            }
        });
        addSlot(new ForgeResultSlot(blockEntity.inventory(), ForgeBlockEntity.SLOT_OUTPUT, 16, 78));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 198));
        }
        addDataSlots(data);
    }

    public ForgeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                findBlockEntity(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(ForgeData.SIZE));
    }

    private static ForgeBlockEntity findBlockEntity(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ForgeBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Forge table block entity missing at " + pos);
    }

    public ForgeBlockEntity blockEntity() {
        return blockEntity;
    }

    public int value() {
        return data.get(ForgeData.VALUE);
    }

    public int strikes() {
        return data.get(ForgeData.STRIKES);
    }

    public int riskPermille() {
        return data.get(ForgeData.RISK_PERMILLE);
    }

    public int tier() {
        return data.get(ForgeData.TIER);
    }

    public boolean frenzyReady() {
        return data.get(ForgeData.FRENZY_READY) != 0;
    }

    public int gainMin() {
        return data.get(ForgeData.GAIN_MIN);
    }

    public int gainMax() {
        return data.get(ForgeData.GAIN_MAX);
    }

    public boolean hasBlade() {
        return slots.get(SLOT_BLADE).hasItem();
    }

    public boolean hasHammer() {
        return slots.get(SLOT_HAMMER).hasItem();
    }

    public boolean outputEmpty() {
        return !slots.get(SLOT_OUTPUT).hasItem();
    }

    public BladeData bladeData() {
        ItemStack stack = blockEntity.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        return stack.isEmpty() ? null : stack.get(ModDataComponents.BLADE_DATA.get());
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 3) {
            if (!moveItemStackTo(stack, 3, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            if (stack.getItem() instanceof BladeBlankItem) {
                moved = moveItemStackTo(stack, SLOT_BLADE, SLOT_BLADE + 1, false);
            } else if (stack.getItem() instanceof HammerItem) {
                moved = moveItemStackTo(stack, SLOT_HAMMER, SLOT_HAMMER + 1, false);
            }
            if (!moved) {
                int start = index < 30 ? 30 : 3;
                int end = index < 30 ? slots.size() : 30;
                if (!moveItemStackTo(stack, start, end, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
