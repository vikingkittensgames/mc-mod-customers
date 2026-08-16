package com.vikingkittens.mc.customers.client.common;

import java.util.List;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils;
import com.vikingkittens.mc.customers.client.compatability.TextureC;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationConfirmPayload;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public class BlockBreakConfirmationScreen extends Screen {
    private static final int WIDTH = 224;
    private static final int HEIGHT = 96;
    private static final long CLOSE_AFTER_MILLIS = 90_000L;
    private static final TextureC TEXTURE = new TextureC(Customers.MODID, "textures/gui/popup_warn.png");
    private final BlockBreakConfirmationPromptPayload payload;
    private int leftPos;
    private int topPos;
    private long closeAtMillis;

    public BlockBreakConfirmationScreen(BlockBreakConfirmationPromptPayload payload) {
        super(Component.translatable(payload.titleKey()));
        this.payload = payload;
    }

    @Override
    protected void init() {
        leftPos = (width - WIDTH) / 2;
        topPos = (height - HEIGHT) / 2;
        closeAtMillis = Util.getMillis() + CLOSE_AFTER_MILLIS;
        addRenderableWidget(Button.builder(Component.translatable("screen.customers.break_confirmation.cancel"), button -> onClose())
                .bounds(leftPos + 8, topPos + 70, 86, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.customers.break_confirmation.confirm"), button -> {
                    CustomersServices.network().sendToServer(new BlockBreakConfirmationConfirmPayload(payload.playerId(), payload.token()));
                    onClose();
                })
                .bounds(leftPos + 98, topPos + 70, 86, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (Util.getMillis() >= closeAtMillis) {
            onClose();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsCUtils.blit(graphics, TEXTURE, leftPos, topPos, 0.0F, 0.0F, WIDTH, HEIGHT, WIDTH, HEIGHT);
        graphics.drawString(font, title, leftPos + 8, topPos + 12, 0xFF000000, false);
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.translatable(payload.messageKey()), WIDTH - 16);
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(font, lines.get(index), leftPos + 8, topPos + 32 + index * font.lineHeight, 0xFF000000, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
