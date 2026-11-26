package com.inf.menu;

/**
 * Pure shift-click routing rules for {@link ForgeMenu}. The menu keeps the actual
 * slot manipulation; this class only decides the target range.
 */
public final class ForgeMenuTransfer {
    public static final int SLOT_BLADE = 0;
    public static final int SLOT_HAMMER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int MACHINE_SLOTS = 3;

    private ForgeMenuTransfer() {
    }

    /** Destination slot range for an item shift-clicked from the player inventory. */
    public static Range destinationFor(boolean blade, boolean hammer) {
        if (blade) {
            return new Range(SLOT_BLADE, SLOT_BLADE + 1);
        }
        if (hammer) {
            return new Range(SLOT_HAMMER, SLOT_HAMMER + 1);
        }
        return null;
    }

    /** Player-inventory ranges used when a machine slot or an unrelated item is moved. */
    public static Range mainInventory(int playerSlotCount) {
        return new Range(MACHINE_SLOTS, playerSlotCount - 9);
    }

    public static Range hotbar(int playerSlotCount) {
        return new Range(playerSlotCount - 9, playerSlotCount);
    }

    public record Range(int start, int end) {
        public Range {
            if (start < 0 || end < start) {
                throw new IllegalArgumentException("invalid range " + start + ".." + end);
            }
        }
    }
}
