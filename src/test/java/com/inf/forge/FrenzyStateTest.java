package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inf.weapon.WeaponKind;
import org.junit.jupiter.api.Test;

class FrenzyStateTest {
    @Test
    void chargesAfterRequiredCompletions() {
        FrenzyState state = FrenzyState.EMPTY;
        for (int i = 0; i < 9; i++) {
            state = state.onWeaponCompleted(WeaponKind.SWORD, 10);
        }
        assertFalse(state.ready());
        state = state.onWeaponCompleted(WeaponKind.SWORD, 10);
        assertTrue(state.ready());
    }

    @Test
    void differentKindsDoNotShareProgress() {
        FrenzyState state = FrenzyState.EMPTY;
        for (int i = 0; i < 9; i++) {
            state = state.onWeaponCompleted(WeaponKind.SWORD, 10);
        }
        state = state.onWeaponCompleted(WeaponKind.AXE, 10);
        assertFalse(state.ready());
    }

    @Test
    void activationClearsProgressAndReady() {
        FrenzyState state = FrenzyState.EMPTY.onWeaponCompleted(WeaponKind.SWORD, 1);
        FrenzyState used = state.activated();
        assertFalse(used.ready());
        assertEquals(0, used.progress(WeaponKind.SWORD));
    }
}
