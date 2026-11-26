package com.inf.client;

import com.inf.Inf;
import com.inf.InfConfig;
import com.inf.forge.ForgeOutcome;
import com.inf.item.component.BladeData;
import com.inf.menu.ForgeMenu;
import com.inf.weapon.WeaponTier;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ForgeScreen extends AbstractContainerScreen<ForgeMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Inf.MODID, "textures/gui/forge_table.png");

    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 260;

    private static final int COLOR_TEXT = 0xFFF0E6D2;
    private static final int COLOR_DIM = 0xFFB3A18A;
    private static final int COLOR_BRASS = 0xFFC9A227;
    private static final int COLOR_BREAK = 0xFFD1453B;
    private static final int COLOR_DAMAGE = 0xFFD9A441;
    private static final int COLOR_SUCCESS = 0xFF8FA35A;
    private static final int COLOR_GREAT = 0xFFFFB15C;

    private Button forgeButton;
    private Button bankButton;
    private Button frenzyButton;
    private float displayedValue;

    public ForgeScreen(ForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
        this.displayedValue = menu.value();
    }

    @Override
    protected void init() {
        super.init();
        forgeButton = addRenderableWidget(new ForgeButton(
                leftPos + 12,
                topPos + 212,
                100,
                26,
                Component.translatable("gui.inf.forge.forge"),
                button -> sendAction(ForgeMenu.ACTION_STRIKE),
                0,
                270));
        bankButton = addRenderableWidget(new ForgeButton(
                leftPos + 118,
                topPos + 212,
                60,
                26,
                Component.translatable("gui.inf.forge.bank"),
                button -> sendAction(ForgeMenu.ACTION_STOP),
                110,
                270));
        frenzyButton = addRenderableWidget(new ForgeButton(
                leftPos + 184,
                topPos + 212,
                56,
                26,
                Component.translatable("gui.inf.forge.frenzy"),
                button -> sendAction(ForgeMenu.ACTION_FRENZY),
                180,
                270));
        updateButtons();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF100C0A);
        renderBg(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
        float target = menu.value();
        if (InfConfig.uiAnimations()) {
            displayedValue += (target - displayedValue) * 0.25F;
        } else {
            displayedValue = target;
        }
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 512);

        int valueWidth = Math.min(88, (int) (displayedValue * 88 / 1000));
        if (valueWidth > 0) {
            graphics.fill(leftPos + 151, topPos + 73, leftPos + 151 + valueWidth, topPos + 79, 0xFFE8622C);
        }

        for (int index = 0; index < 6; index++) {
            int x = leftPos + 152 + index * 14;
            int y = topPos + 150;
            boolean lit = menu.hasBlade() && index <= menu.tier();
            graphics.fill(x, y, x + 7, y + 7, lit ? 0xFFC9A227 : 0xFF2B221C);
            graphics.fill(x + 1, y + 1, x + 6, y + 6, lit ? 0xFFF0D37A : 0xFF16110E);
        }

        drawChancePanel(graphics);
    }

    private void drawChancePanel(GuiGraphics graphics) {
        int x = leftPos + 250;
        int y = topPos + 34;
        graphics.fill(x, y, x + 160, y + 134, 0xFF100C0A);
        graphics.fill(x, y, x + 160, y + 1, 0xFF3A2C22);
        graphics.fill(x, y + 133, x + 160, y + 134, 0xFF050403);
        graphics.fill(x, y, x + 1, y + 134, 0xFF3A2C22);
        graphics.fill(x + 159, y, x + 160, y + 134, 0xFF050403);

        int[] values = {menu.breakPermille(), menu.damagePermille(), menu.successPermille(), menu.greatPermille()};
        int[] colors = {COLOR_BREAK, COLOR_DAMAGE, COLOR_SUCCESS, COLOR_GREAT};
        for (int index = 0; index < 4; index++) {
            int rowY = y + 22 + index * 27;
            int barWidth = Math.min(140, values[index] * 140 / 1000);
            graphics.fill(x + 10, rowY + 10, x + 150, rowY + 14, 0xFF050403);
            if (barWidth > 0) {
                graphics.fill(x + 10, rowY + 10, x + 10 + barWidth, rowY + 14, colors[index]);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("block.inf.forge_table"), 14, 10, COLOR_BRASS, false);
        graphics.drawString(font, Component.translatable("gui.inf.forge.subtitle"), 14, 22, COLOR_DIM, false);
        String brand = Component.translatable("gui.inf.forge.brand").getString();
        graphics.drawString(font, brand, 406 - font.width(brand), 10, 0xFF6B6157, false);

        graphics.drawString(font, Component.translatable("gui.inf.forge.slot_blade"), 48, 53, COLOR_DIM, false);
        graphics.drawString(font, Component.translatable("gui.inf.forge.slot_hammer"), 48, 83, COLOR_DIM, false);
        graphics.drawString(font, Component.translatable("gui.inf.forge.slot_output"), 48, 113, COLOR_DIM, false);

        graphics.drawString(font, Component.translatable("gui.inf.forge.chances"), 258, 40, COLOR_BRASS, false);
        drawChanceRows(graphics);

        BladeData blade = menu.bladeData();
        boolean ready = menu.hasBlade() && menu.hasHammer();

        if (blade != null) {
            WeaponTier tier = WeaponTier.forValue(blade.value());
            Component name = Component.translatable(
                    "inf.weapon.name",
                    Component.translatable(tier.translationKey()),
                    Component.translatable(blade.kind().translationKey()));
            graphics.drawString(font, name, 150, 44, COLOR_TEXT, false);
        }

        graphics.drawString(
                font, Component.translatable("gui.inf.forge.value", menu.value()), 150, 60, COLOR_TEXT, false);
        graphics.drawString(font, Component.translatable("gui.inf.forge.tier"), 150, 140, COLOR_DIM, false);

        if (!ready) {
            graphics.drawString(font, Component.translatable("gui.inf.forge.empty_hint"), 150, 92, COLOR_DIM, false);
            return;
        }

        graphics.drawString(
                font, Component.translatable("gui.inf.forge.strikes", menu.strikes()), 150, 92, COLOR_DIM, false);
        graphics.drawString(
                font,
                Component.translatable("gui.inf.forge.gain", menu.gainMin(), menu.gainMax()),
                150,
                106,
                COLOR_DIM,
                false);
        Component frenzyText;
        if (menu.frenzyActive() > 0) {
            frenzyText = Component.translatable("gui.inf.forge.frenzy_active", menu.frenzyActive());
        } else if (menu.frenzyReady()) {
            frenzyText = Component.translatable("gui.inf.forge.frenzy_ready");
        } else {
            frenzyText = Component.translatable(
                    "gui.inf.forge.frenzy_progress", menu.frenzyProgress(), menu.frenzyRequired());
        }
        graphics.drawString(font, frenzyText, 150, 120, COLOR_DIM, false);
    }

    private void drawChanceRows(GuiGraphics graphics) {
        ForgeOutcome[] outcomes = {
            ForgeOutcome.BREAK, ForgeOutcome.DAMAGE, ForgeOutcome.SUCCESS, ForgeOutcome.GREAT_SUCCESS
        };
        int[] values = {menu.breakPermille(), menu.damagePermille(), menu.successPermille(), menu.greatPermille()};
        int[] colors = {COLOR_BREAK, COLOR_DAMAGE, COLOR_SUCCESS, COLOR_GREAT};
        for (int index = 0; index < outcomes.length; index++) {
            int rowY = 56 + index * 27;
            graphics.drawString(
                    font, Component.translatable(outcomes[index].translationKey()), 258, rowY, colors[index], false);
            String percent = String.format(Locale.ROOT, "%.1f%%", values[index] / 10.0);
            graphics.drawString(font, percent, 400 - font.width(percent), rowY, COLOR_TEXT, false);
        }
    }
}
