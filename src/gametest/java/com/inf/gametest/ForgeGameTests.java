package com.inf.gametest;

import com.inf.Inf;
import com.inf.block.ForgeBlockEntity;
import com.inf.block.ForgeData;
import com.inf.forge.ForgeService;
import com.inf.forge.FrenzyState;
import com.inf.item.component.BladeData;
import com.inf.menu.ForgeMenu;
import com.inf.registry.ModAttachments;
import com.inf.registry.ModBlocks;
import com.inf.registry.ModDataComponents;
import com.inf.registry.ModItems;
import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponTier;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Inf.MODID)
@PrefixGameTestTemplate(false)
public final class ForgeGameTests {
    private static final BlockPos TABLE = new BlockPos(1, 1, 1);

    private ForgeGameTests() {}

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void strike_success_updatesBladeAndWearsHammer(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        helper.assertTrue(ForgeService.strike(forge, player, 0.999, 50), "strike should resolve");

        BladeData data = bladeData(forge);
        helper.assertTrue(data != null && data.value() > 0, "blade value should rise");
        helper.assertTrue(data.strikes() == 1, "one strike should be recorded");
        helper.assertTrue(hammer(forge).getDamageValue() == 1, "hammer should wear by one");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void strike_break_clearsBladeAndWearsHammer(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        helper.assertTrue(ForgeService.strike(forge, player, 0.0, 50), "break strike should resolve");
        helper.assertTrue(
                forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE).isEmpty(),
                "broken blade should be removed");
        helper.assertTrue(hammer(forge).getDamageValue() == 1, "hammer should still wear");
        helper.assertTrue(forge.frenzyStrikesLeft() == 0, "break resets frenzy");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void strike_damage_losesTenPercent(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        setBlade(forge, new BladeData(WeaponKind.SWORD, 100, 10, 0L));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        double roll = com.inf.forge.ForgeRates.of(100, 10, false, com.inf.forge.ForgeBalance.DEFAULT)
                .breakChance();

        helper.assertTrue(ForgeService.strike(forge, player, roll, 50), "damage strike should resolve");
        BladeData data = bladeData(forge);
        helper.assertTrue(data != null && data.value() == 90, "value should drop by ten percent");
        helper.assertTrue(data.strikes() == 11, "strike count should rise");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void hammer_lastDurability_breaksAndEmptiesSlot(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 127);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        helper.assertTrue(ForgeService.strike(forge, player, 0.999, 50), "last strike should resolve");
        helper.assertTrue(
                forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_HAMMER).isEmpty(),
                "worn hammer should disappear");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void stop_banksWeaponWithTierComponents(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        setBlade(forge, new BladeData(WeaponKind.SWORD, 1000, 20, 0L));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        helper.assertTrue(ForgeService.stop(forge, player), "stop should bank the weapon");
        ItemStack weapon = forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_OUTPUT);
        helper.assertTrue(weapon.getItem() == ModItems.FORGED_SWORD.get(), "wrong output item");
        helper.assertTrue(weapon.get(DataComponents.RARITY) == Rarity.EPIC, "legendary uses EPIC rarity");
        helper.assertTrue(weapon.get(DataComponents.ITEM_NAME) != null, "weapon should be named");
        ItemAttributeModifiers modifiers = weapon.get(DataComponents.ATTRIBUTE_MODIFIERS);
        helper.assertTrue(modifiers != null, "weapon should carry attributes");
        double damage = modifiers.modifiers().stream()
                .filter(entry -> entry.attribute().is(Attributes.ATTACK_DAMAGE))
                .mapToDouble(entry -> entry.modifier().amount())
                .findFirst()
                .orElse(-1);
        helper.assertTrue(damage == 11.0, "legendary sword damage modifier should be 11, got " + damage);
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void stop_pickaxeToolComponentGatesDrops(GameTestHelper helper) {
        ItemStack crude = com.inf.forge.WeaponFactory.create(WeaponKind.PICKAXE, 0, 0, 0L);
        ItemStack legendary = com.inf.forge.WeaponFactory.create(WeaponKind.PICKAXE, 1000, 0, 0L);

        Tool crudeTool = crude.get(DataComponents.TOOL);
        Tool legendaryTool = legendary.get(DataComponents.TOOL);
        helper.assertTrue(crudeTool != null && legendaryTool != null, "pickaxes should carry a tool component");
        helper.assertTrue(crudeTool.rules().size() == 3, "crude denies iron and diamond, then mines");
        helper.assertTrue(legendaryTool.rules().size() == 1, "legendary only mines");
        helper.assertTrue(legendaryTool.defaultMiningSpeed() == 1.0F, "default mining speed");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void stop_rejectedWhenOutputOccupiedOrValueZero(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        helper.assertFalse(ForgeService.stop(forge, player), "zero value cannot be banked");
        setBlade(forge, new BladeData(WeaponKind.SWORD, 500, 5, 0L));
        forge.inventory().setStackInSlot(ForgeBlockEntity.SLOT_OUTPUT, new ItemStack(ModItems.HAMMER.get()));
        helper.assertFalse(ForgeService.stop(forge, player), "occupied output cannot be banked");
        helper.assertTrue(bladeData(forge) != null, "blade must survive a rejected stop");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 400)
    public static void frenzy_grantsThreeUnbreakableStrikes(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        setBlade(forge, new BladeData(WeaponKind.SWORD, 50, 5, 0L));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setData(ModAttachments.FRENZY, new FrenzyState(java.util.Map.of(), true));

        helper.assertTrue(ForgeService.activateFrenzy(forge, player), "frenzy should activate");
        int granted = forge.frenzyStrikesLeft();
        helper.assertTrue(
                granted == com.inf.InfConfig.frenzyFreeStrikes(),
                "grant should match the configured free strikes, got " + granted);
        helper.assertTrue(granted > 0, "frenzy should grant at least one strike");

        for (int strike = 0; strike < granted; strike++) {
            forge.markStrike(Long.MIN_VALUE);
            helper.assertTrue(ForgeService.strike(forge, player, 0.0, 50), "frenzy strike " + strike);
            helper.assertTrue(bladeData(forge) != null, "frenzy must never break the blade");
        }
        helper.assertTrue(forge.frenzyStrikesLeft() == 0, "frenzy should be consumed");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void frenzy_activationRequiresReadyStateAndValue(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        helper.assertFalse(ForgeService.activateFrenzy(forge, player), "not ready");
        player.setData(ModAttachments.FRENZY, new FrenzyState(java.util.Map.of(), true));
        helper.assertFalse(ForgeService.activateFrenzy(forge, player), "zero value");
        setBlade(forge, new BladeData(WeaponKind.SWORD, 10, 1, 0L));
        helper.assertTrue(ForgeService.activateFrenzy(forge, player), "ready and valued");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void menu_buttonDispatch_strike(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ForgeMenu menu = new ForgeMenu(
                0, player.getInventory(), forge, new ForgeData(forge, player, com.inf.forge.ForgeBalance.DEFAULT));

        helper.assertTrue(menu.clickMenuButton(player, ForgeMenu.ACTION_STRIKE), "strike action");
        helper.assertTrue(bladeData(forge) != null && bladeData(forge).strikes() == 1, "menu strike lands");
        helper.assertFalse(menu.clickMenuButton(player, 99), "unknown action is rejected");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void menu_quickMoveRoutesBlanksAndHammers(GameTestHelper helper) {
        ForgeBlockEntity forge = placeEmptyForge(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ForgeMenu menu = new ForgeMenu(
                0, player.getInventory(), forge, new ForgeData(forge, player, com.inf.forge.ForgeBalance.DEFAULT));

        player.getInventory().setItem(9, new ItemStack(ModItems.SWORD_BLANK.get()));
        menu.quickMoveStack(player, 3);
        helper.assertTrue(
                forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE).getItem() == ModItems.SWORD_BLANK.get(),
                "blank should route to the blade slot");

        player.getInventory().setItem(10, new ItemStack(ModItems.HAMMER.get()));
        menu.quickMoveStack(player, 4);
        helper.assertTrue(
                forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_HAMMER).getItem() == ModItems.HAMMER.get(),
                "hammer should route to the hammer slot");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void blockEntity_saveLoad_roundTripsAllState(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 3);
        setBlade(forge, new BladeData(WeaponKind.AXE, 240, 12, 99L));
        forge.setFrenzyStrikesLeft(2);
        forge.markStrike(42L);

        CompoundTag tag = forge.saveWithFullMetadata(helper.getLevel().registryAccess());
        ForgeBlockEntity loaded = (ForgeBlockEntity) net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                forge.getBlockPos(),
                forge.getBlockState(),
                tag,
                helper.getLevel().registryAccess());

        helper.assertTrue(loaded != null, "block entity should load");
        BladeData loadedBlade =
                loaded.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE).get(ModDataComponents.BLADE_DATA.get());
        helper.assertTrue(
                loadedBlade != null && loadedBlade.value() == 240 && loadedBlade.strikes() == 12,
                "blade data should survive");
        helper.assertTrue(loaded.frenzyStrikesLeft() == 2, "frenzy strikes should survive");
        helper.assertTrue(loaded.lastStrikeGameTime() == 42L, "clock should survive");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void forgeTable_removal_dropsContents(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        setBlade(forge, new BladeData(WeaponKind.SWORD, 100, 3, 0L));
        forge.inventory().setStackInSlot(ForgeBlockEntity.SLOT_OUTPUT, new ItemStack(ModItems.HAMMER.get()));

        helper.destroyBlock(TABLE);
        helper.assertItemEntityPresent(ModItems.SWORD_BLANK.get(), TABLE, 1.0);
        helper.assertItemEntityPresent(ModItems.HAMMER.get(), TABLE, 1.0);
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void recipes_everyInfRecipeLoads(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
        for (String id : List.of(
                "hammer", "sword_blank", "pickaxe_blank", "axe_blank", "scythe_blank", "dagger_blank", "forge_table")) {
            RecipeHolder<?> holder = recipes.byKey(ResourceLocation.fromNamespaceAndPath(Inf.MODID, id))
                    .orElseThrow(() -> new AssertionError("missing recipe " + id));
            helper.assertTrue(
                    holder.value().getResultItem(helper.getLevel().registryAccess()) != null,
                    "recipe result for " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void advancements_allNineteenExist(GameTestHelper helper) {
        for (String id : com.inf.advancement.ForgeAdvancements.allIds()) {
            helper.assertTrue(
                    helper.getLevel()
                                    .getServer()
                                    .getAdvancements()
                                    .get(ResourceLocation.fromNamespaceAndPath(Inf.MODID, id))
                            != null,
                    "missing advancement " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void advancements_awardOnStrikeAndStop(GameTestHelper helper) {
        ForgeBlockEntity forge = placeForge(helper, ModItems.SWORD_BLANK.get(), 0);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ForgeService.strike(forge, player, 0.999, 50);
        helper.assertTrue(
                com.inf.advancement.ForgeAdvancements.done(player, com.inf.advancement.ForgeAdvancements.FIRST_STRIKE),
                "first_strike should be awarded");

        setBlade(forge, new BladeData(WeaponKind.DAGGER, 1000, 20, 0L));
        helper.assertTrue(ForgeService.stop(forge, player), "banking should succeed");
        helper.assertTrue(
                com.inf.advancement.ForgeAdvancements.done(player, com.inf.advancement.ForgeAdvancements.FIRST_WEAPON),
                "first_weapon should be awarded");
        helper.assertTrue(
                com.inf.advancement.ForgeAdvancements.done(
                        player, com.inf.advancement.ForgeAdvancements.tier(WeaponTier.LEGENDARY)),
                "tier_legendary");
        helper.assertTrue(
                com.inf.advancement.ForgeAdvancements.done(
                        player, com.inf.advancement.ForgeAdvancements.kind(WeaponKind.DAGGER)),
                "kind_dagger");
        helper.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void knockback_suppressedForDaggerOnly(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 1, 2);
        zombie.setLastHurtByMob(player);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FORGED_DAGGER.get()));
        LivingKnockBackEvent dagger = new LivingKnockBackEvent(zombie, 1.0F, 1.0, 1.0);
        com.inf.combat.CombatEvents.onKnockback(dagger);
        helper.assertTrue(dagger.getStrength() == 0.0F, "dagger should suppress knockback");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FORGED_SWORD.get()));
        LivingKnockBackEvent sword = new LivingKnockBackEvent(zombie, 1.0F, 1.0, 1.0);
        com.inf.combat.CombatEvents.onKnockback(sword);
        helper.assertTrue(sword.getStrength() == 1.0F, "sword should keep knockback");
        helper.succeed();
    }

    private static ForgeBlockEntity placeEmptyForge(GameTestHelper helper) {
        helper.setBlock(TABLE, ModBlocks.FORGE_TABLE.get());
        return helper.getBlockEntity(TABLE);
    }

    private static ForgeBlockEntity placeForge(
            GameTestHelper helper, net.minecraft.world.item.Item blade, int hammerDamage) {
        helper.setBlock(TABLE, ModBlocks.FORGE_TABLE.get());
        ForgeBlockEntity forge = helper.getBlockEntity(TABLE);
        forge.inventory().setStackInSlot(ForgeBlockEntity.SLOT_BLADE, new ItemStack(blade));
        ItemStack hammer = new ItemStack(ModItems.HAMMER.get());
        hammer.setDamageValue(hammerDamage);
        forge.inventory().setStackInSlot(ForgeBlockEntity.SLOT_HAMMER, hammer);
        return forge;
    }

    private static void setBlade(ForgeBlockEntity forge, BladeData data) {
        ItemStack blade = forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        blade.set(ModDataComponents.BLADE_DATA.get(), data);
    }

    private static BladeData bladeData(ForgeBlockEntity forge) {
        ItemStack blade = forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_BLADE);
        return blade.isEmpty() ? null : blade.get(ModDataComponents.BLADE_DATA.get());
    }

    private static ItemStack hammer(ForgeBlockEntity forge) {
        return forge.inventory().getStackInSlot(ForgeBlockEntity.SLOT_HAMMER);
    }
}
