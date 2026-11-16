package com.inf.forge;

public enum ForgeOutcome {
    BREAK("break"),
    DAMAGE("damage"),
    SUCCESS("success"),
    GREAT_SUCCESS("great_success");

    public final String id;

    ForgeOutcome(String id) {
        this.id = id;
    }

    public String translationKey() {
        return "inf.outcome." + id;
    }
}
