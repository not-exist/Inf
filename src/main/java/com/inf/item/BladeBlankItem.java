package com.inf.item;

import com.inf.weapon.WeaponKind;
import net.minecraft.world.item.Item;

public class BladeBlankItem extends Item {
    private final WeaponKind kind;

    public BladeBlankItem(WeaponKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public WeaponKind kind() {
        return kind;
    }
}
