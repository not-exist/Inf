package com.inf.registry;

import com.inf.Inf;
import com.inf.item.BladeBlankItem;
import com.inf.item.HammerItem;
import com.inf.item.forged.ForgedAxeItem;
import com.inf.item.forged.ForgedDaggerItem;
import com.inf.item.forged.ForgedPickaxeItem;
import com.inf.item.forged.ForgedScytheItem;
import com.inf.item.forged.ForgedSwordItem;
import com.inf.weapon.WeaponKind;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Inf.MODID);

    public static final DeferredItem<HammerItem> HAMMER = ITEMS.registerItem(
            "hammer",
            HammerItem::new,
            new Item.Properties().durability(HammerItem.DURABILITY).stacksTo(1));

    public static final DeferredItem<BladeBlankItem> SWORD_BLANK = ITEMS.register(
            "sword_blank", () -> new BladeBlankItem(WeaponKind.SWORD, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BladeBlankItem> PICKAXE_BLANK = ITEMS.register(
            "pickaxe_blank", () -> new BladeBlankItem(WeaponKind.PICKAXE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BladeBlankItem> AXE_BLANK =
            ITEMS.register("axe_blank", () -> new BladeBlankItem(WeaponKind.AXE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BladeBlankItem> SCYTHE_BLANK = ITEMS.register(
            "scythe_blank", () -> new BladeBlankItem(WeaponKind.SCYTHE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BladeBlankItem> DAGGER_BLANK = ITEMS.register(
            "dagger_blank", () -> new BladeBlankItem(WeaponKind.DAGGER, new Item.Properties().stacksTo(1)));

    public static final DeferredItem<ForgedSwordItem> FORGED_SWORD =
            ITEMS.register("forged_sword", () -> new ForgedSwordItem(new Item.Properties()));
    public static final DeferredItem<ForgedPickaxeItem> FORGED_PICKAXE =
            ITEMS.register("forged_pickaxe", () -> new ForgedPickaxeItem(new Item.Properties()));
    public static final DeferredItem<ForgedAxeItem> FORGED_AXE =
            ITEMS.register("forged_axe", () -> new ForgedAxeItem(new Item.Properties()));
    public static final DeferredItem<ForgedScytheItem> FORGED_SCYTHE =
            ITEMS.register("forged_scythe", () -> new ForgedScytheItem(new Item.Properties()));
    public static final DeferredItem<ForgedDaggerItem> FORGED_DAGGER =
            ITEMS.register("forged_dagger", () -> new ForgedDaggerItem(new Item.Properties()));

    private ModItems() {}

    public static Item forged(WeaponKind kind) {
        return switch (kind) {
            case SWORD -> FORGED_SWORD.get();
            case PICKAXE -> FORGED_PICKAXE.get();
            case AXE -> FORGED_AXE.get();
            case SCYTHE -> FORGED_SCYTHE.get();
            case DAGGER -> FORGED_DAGGER.get();
        };
    }
}
