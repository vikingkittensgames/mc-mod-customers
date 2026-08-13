package com.vikingkittens.mc.customers.client.compatability;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

/**
 * Provides version-compatible rendering for vanilla-style boss bars.
 */
public final class BossBarCUtils {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int OVERLAY_OFFSET = 80;
    private static final ResourceLocation GUI_BARS_LOCATION =
            new ResourceLocation("textures/gui/bars.png");

    private BossBarCUtils() {
    }

    /**
     * Renders a boss bar using its configured color, overlay, and progress.
     *
     * @param graphics GUI rendering context
     * @param x left coordinate
     * @param y top coordinate
     * @param bossEvent boss bar state
     */
    public static void render(
            GuiGraphics graphics,
            int x,
            int y,
            BossEvent bossEvent
    ) {
        render(
                graphics,
                x,
                y,
                bossEvent,
                BAR_WIDTH,
                0
        );
        int progress = (int) (bossEvent.getProgress() * BAR_WIDTH);
        if (progress > 0) {
            render(
                    graphics,
                    x,
                    y,
                    bossEvent,
                    progress,
                    BAR_HEIGHT
            );
        }
    }

    private static void render(
            GuiGraphics graphics,
            int x,
            int y,
            BossEvent bossEvent,
            int width,
            int verticalOffset
    ) {
        RenderSystem.enableBlend();
        graphics.blit(
                GUI_BARS_LOCATION,
                x,
                y,
                0,
                bossEvent.getColor().ordinal() * BAR_HEIGHT * 2 + verticalOffset,
                width,
                BAR_HEIGHT
        );
        if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            graphics.blit(
                    GUI_BARS_LOCATION,
                    x,
                    y,
                    0,
                    OVERLAY_OFFSET + (bossEvent.getOverlay().ordinal() - 1) * BAR_HEIGHT * 2
                            + verticalOffset,
                    width,
                    BAR_HEIGHT
            );
        }
        RenderSystem.disableBlend();
    }
}
