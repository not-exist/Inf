package com.inf.client;

import com.inf.Inf;
import com.inf.InfConfig;
import com.inf.item.component.BladeData;
import com.inf.menu.ForgeMenu;
import com.inf.weapon.WeaponTier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ForgeScreen extends AbstractContainerScreen<ForgeMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Inf.MODID, "textures/gui/forge_table.png");

    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 222;

    private Button forgeButton;
    private Button bankButton;
    private Button frenzyButton;

    public ForgeScreen(ForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        forgeButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.inf.forge.forge"),
                        button -> sendAction(ForgeMenu.ACTION_STRIKE))
                .bounds(leftPos + 16, topPos + 118, 72, 20)
                .build());
        bankButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.inf.forge.bank"),
                        button -> sendAction(ForgeMenu.ACTION_STOP))
                .bounds(leftPos + 92, topPos + 118, 36, 20)
                .build());
        frenzyButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.inf.forge.frenzy"),
                        button -> sendAction(ForgeMenu.ACTION_FRENZY))
                .bounds(leftPos + 132, topPos + 118, 28, 20)
                .build());
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        forgeButton.active = menu.hasBlade() && menu.hasHammer();
        bankButton.active = menu.hasBlade() && menu.outputEmpty() && menu.value() > 0;
        frenzyButton.active = menu.frenzyReady() && menu.hasBlade() && menu.value() > 0;
    }

    private void sendAction(int action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int valueWidth = Math.min(116, menu.value() * 116 / 1000);
        if (valueWidth > 0) {
            graphics.fill(leftPos + 45, topPos + 51, leftPos + 45 + valueWidth, topPos + 55, 0xFFE8622C);
        }

        int riskWidth = Math.min(116, menu.riskPermille() * 116 / 1000);
        if (riskWidth > 0) {
            graphics.fill(leftPos + 45, topPos + 73, leftPos + 45 + riskWidth, topPos + 74, riskColor());
        }
    }

    private int riskColor() {
        int risk = menu.riskPermille();
        if (risk < 200) {
            return 0xFF8FA35A;
        }
        if (risk < 450) {
            return 0xFFD9A441;
        }
        return 0xFFD1453B;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("block.inf.forge_table"), 8, 8, 0xFFC9A227, false);

        BladeData blade = menu.bladeData();
        if (blade != null) {
            WeaponTier tier = WeaponTier.forValue(blade.value());
            Component name = Component.translatable(
                    "inf.weapon.name",
                    Component.translatable(tier.translationKey()),
                    Component.translatable(blade.kind().translationKey()));
            graphics.drawString(font, name, 44, 28, 0xFFF0E6D2, false);
        }

        graphics.drawString(
                font,
                Component.translatable("gui.inf.forge.value", menu.value()),
                44,
                40,
                0xFFF0E6D2,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.inf.forge.risk", String.format(java.util.Locale.ROOT, "%.1f%%", menu.riskPermille() / 10.0)),
                44,
                62,
                riskColor(),
                false);
        graphics.drawString(
                font,
                Component.translatable("gui.inf.forge.strikes", menu.strikes()),
                44,
                82,
                0xFFB3A18A,
                false);
        graphics.drawString(
                font,
                Component.translatable("gui.inf.forge.gain", menu.gainMin(), menu.gainMax()),
                44,
                94,
                0xFFB3A18A,
                false);
        Component frenzyText;
        if (menu.frenzyActive() > 0) {
            frenzyText = Component.translatable("gui.inf.forge.frenzy_active", menu.frenzyActive());
        } else if (menu.frenzyReady()) {
            frenzyText = Component.translatable("gui.inf.forge.frenzy_ready");
        } else {
            frenzyText = Component.translatable(
                    "gui.inf.forge.frenzy_progress", menu.frenzyProgress(), InfConfig.frenzyRequired());
        }
        graphics.drawString(font, frenzyText, 44, 106, 0xFFB3A18A, false);

        if (!menu.hasBlade() || !menu.hasHammer()) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.inf.forge.empty_hint"),
                    8,
                    108,
                    0xFFB3A18A,
                    false);
        }
    }
}
