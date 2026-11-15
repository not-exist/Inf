package com.inf.weapon;

public enum WeaponKind {
    SWORD("sword"),
    PICKAXE("pickaxe"),
    AXE("axe"),
    SCYTHE("scythe"),
    DAGGER("dagger");

    public final String id;

    WeaponKind(String id) {
        this.id = id;
    }

    public String translationKey() {
        return "inf.weapon." + id;
    }

    public static WeaponKind byId(String id) {
        for (WeaponKind kind : values()) {
            if (kind.id.equals(id)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown weapon kind: " + id);
    }
}
