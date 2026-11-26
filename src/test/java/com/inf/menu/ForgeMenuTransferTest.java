package com.inf.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ForgeMenuTransferTest {
    private static final int SLOT_COUNT = 39;

    @Test
    void blankGoesToTheBladeSlot() {
        assertEquals(new ForgeMenuTransfer.Range(0, 1), ForgeMenuTransfer.destinationFor(true, false));
    }

    @Test
    void hammerGoesToTheHammerSlot() {
        assertEquals(new ForgeMenuTransfer.Range(1, 2), ForgeMenuTransfer.destinationFor(false, true));
    }

    @Test
    void unrelatedItemHasNoMachineDestination() {
        assertNull(ForgeMenuTransfer.destinationFor(false, false));
    }

    @Test
    void mainInventory_excludesTheHotbar() {
        assertEquals(new ForgeMenuTransfer.Range(3, 30), ForgeMenuTransfer.mainInventory(SLOT_COUNT));
    }

    @Test
    void hotbar_isTheLastNineSlots() {
        assertEquals(new ForgeMenuTransfer.Range(30, 39), ForgeMenuTransfer.hotbar(SLOT_COUNT));
    }

    @Test
    void invalidRange_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ForgeMenuTransfer.Range(5, 4));
        assertThrows(IllegalArgumentException.class, () -> new ForgeMenuTransfer.Range(-1, 3));
    }
}
