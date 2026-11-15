package com.inf.client;

import com.inf.Inf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ForgeButton extends Button {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Inf.MODID, "textures/gui/forge_table.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int STATE_OFFSET = 22;

    private final int u;
    private final int v;

    public ForgeButton(int x, int y, int width, int height, Component message, OnPress onPress, int u, int v) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.u = u;
        this.v = v;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int stateV = active && isHoveredOrFocused() ? v + STATE_OFFSET : v;
        graphics.blit(TEXTURE, getX(), getY(), u, stateV, getWidth(), getHeight(), TEXTURE_SIZE, TEXTURE_SIZE);
        if (!active) {
            graphics.fill(
                    getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0x66000000);
        }
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                active ? 0xFFF0E6D2 : 0xFF6B6157);
    }
}
