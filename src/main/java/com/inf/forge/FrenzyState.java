package com.inf.forge;

import com.inf.weapon.WeaponKind;
import java.util.EnumMap;
import java.util.Map;

public record FrenzyState(Map<WeaponKind, Integer> progress, boolean ready) {
    public static final FrenzyState EMPTY = new FrenzyState(Map.of(), false);

    public FrenzyState onWeaponCompleted(WeaponKind kind, int required) {
        if (ready) {
            return this;
        }
        int next = progress.getOrDefault(kind, 0) + 1;
        EnumMap<WeaponKind, Integer> updated = new EnumMap<>(WeaponKind.class);
        updated.putAll(progress);
        updated.put(kind, next);
        return new FrenzyState(Map.copyOf(updated), next >= required);
    }

    public FrenzyState activated() {
        return EMPTY;
    }

    public int progress(WeaponKind kind) {
        return progress.getOrDefault(kind, 0);
    }
}
