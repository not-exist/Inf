package com.inf.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WeaponKindTest {
    @Test
    void byId_roundTripsEveryKind() {
        for (WeaponKind kind : WeaponKind.values()) {
            assertEquals(kind, WeaponKind.byId(kind.id));
        }
    }

    @Test
    void byId_unknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> WeaponKind.byId("warpaxe"));
    }

    @Test
    void translationKeys_areStable() {
        for (WeaponKind kind : WeaponKind.values()) {
            assertEquals("inf.weapon." + kind.id, kind.translationKey());
        }
    }
}
