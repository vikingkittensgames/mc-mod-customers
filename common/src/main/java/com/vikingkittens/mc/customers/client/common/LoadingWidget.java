package com.vikingkittens.mc.customers.client.common;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LoadingWidget {
    private static final long CYCLE_MILLIS = 2000L;
    private static final double EXTENT = 32.0D;
    private static final ItemStack ICON = new ItemStack(Items.EMERALD);

    private final Component text;

    public LoadingWidget(Component text) {
        this.text = text;
    }

    public void render(GuiGraphics graphics, Font font, int centerX, int centerY) {
        double angle = (Util.getMillis() % CYCLE_MILLIS) * (Math.PI * 2.0D) / CYCLE_MILLIS;
        double sine = Math.sin(angle);
        double cosine = Math.cos(angle);
        double denominator = 1.0D + sine * sine;
        int iconX = centerX + (int)Math.round(EXTENT * cosine / denominator) - 8;
        int iconY = centerY + (int)Math.round(EXTENT * sine * cosine / denominator) - 8;

        graphics.renderItem(ICON, iconX, iconY);
        graphics.drawString(font, text, centerX - font.width(text) / 2, centerY + 20, 0x000000, false);
    }
}
