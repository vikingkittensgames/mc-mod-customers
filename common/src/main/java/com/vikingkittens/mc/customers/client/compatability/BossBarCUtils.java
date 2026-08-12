package com.vikingkittens.mc.customers.client.compatability;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * Provides version-compatible rendering for vanilla-style boss bars.
 */
public final class BossBarCUtils {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final ResourceLocation[] BAR_BACKGROUND_SPRITES = {
        sprite("pink_background"),
        sprite("blue_background"),
        sprite("red_background"),
        sprite("green_background"),
        sprite("yellow_background"),
        sprite("purple_background"),
        sprite("white_background")
    };
    private static final ResourceLocation[] BAR_PROGRESS_SPRITES = {
        sprite("pink_progress"),
        sprite("blue_progress"),
        sprite("red_progress"),
        sprite("green_progress"),
        sprite("yellow_progress"),
        sprite("purple_progress"),
        sprite("white_progress")
    };
    private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES = {
        sprite("notched_6_background"),
        sprite("notched_10_background"),
        sprite("notched_12_background"),
        sprite("notched_20_background")
    };
    private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES = {
        sprite("notched_6_progress"),
        sprite("notched_10_progress"),
        sprite("notched_12_progress"),
        sprite("notched_20_progress")
    };

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
                BAR_BACKGROUND_SPRITES,
                OVERLAY_BACKGROUND_SPRITES
        );
        int progress = Mth.lerpDiscrete(bossEvent.getProgress(), 0, BAR_WIDTH);
        if (progress > 0) {
            render(
                    graphics,
                    x,
                    y,
                    bossEvent,
                    progress,
                    BAR_PROGRESS_SPRITES,
                    OVERLAY_PROGRESS_SPRITES
            );
        }
    }

    private static void render(
            GuiGraphics graphics,
            int x,
            int y,
            BossEvent bossEvent,
            int width,
            ResourceLocation[] barSprites,
            ResourceLocation[] overlaySprites
    ) {
        RenderSystem.enableBlend();
        graphics.blitSprite(
                barSprites[bossEvent.getColor().ordinal()],
                BAR_WIDTH,
                BAR_HEIGHT,
                0,
                0,
                x,
                y,
                width,
                BAR_HEIGHT
        );
        if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            graphics.blitSprite(
                    overlaySprites[bossEvent.getOverlay().ordinal() - 1],
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    0,
                    0,
                    x,
                    y,
                    width,
                    BAR_HEIGHT
            );
        }
        RenderSystem.disableBlend();
    }

    private static ResourceLocation sprite(String name) {
        return ResourceLocation.withDefaultNamespace("boss_bar/" + name);
    }
}
