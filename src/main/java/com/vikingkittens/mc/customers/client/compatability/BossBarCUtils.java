package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * Renders the vanilla boss-bar background and progress sprites through version-specific GUI APIs.
 */
public final class BossBarCUtils {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final Identifier[] BACKGROUND_SPRITES = createSprites("_background");
    private static final Identifier[] PROGRESS_SPRITES = createSprites("_progress");
    private static final Identifier[] OVERLAY_BACKGROUND_SPRITES = createOverlaySprites("_background");
    private static final Identifier[] OVERLAY_PROGRESS_SPRITES = createOverlaySprites("_progress");

    private BossBarCUtils() {
    }

    /**
     * Renders a vanilla boss bar at the requested position and width.
     *
     * @param graphics GUI drawing context
     * @param x left edge of the bar
     * @param y top edge of the bar
     * @param event boss event supplying color, overlay, and progress
     */
    public static void render(GuiGraphics graphics, int x, int y, BossEvent event) {
        render(graphics, x, y, event, BAR_WIDTH, BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
        int progressWidth = Mth.lerpDiscrete(event.getProgress(), 0, BAR_WIDTH);
        if (progressWidth > 0) {
            render(graphics, x, y, event, progressWidth, PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
        }
    }

    private static void render(
            GuiGraphics graphics,
            int x,
            int y,
            BossEvent event,
            int width,
            Identifier[] colorSprites,
            Identifier[] overlaySprites
    ) {
        blitSprite(graphics, colorSprites[event.getColor().ordinal()], x, y, width);
        if (event.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            blitSprite(graphics, overlaySprites[event.getOverlay().ordinal() - 1], x, y, width);
        }
    }

    private static void blitSprite(GuiGraphics graphics, Identifier sprite, int x, int y, int width) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite,
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

    private static Identifier[] createSprites(String suffix) {
        return new Identifier[] {
                sprite("pink" + suffix),
                sprite("blue" + suffix),
                sprite("red" + suffix),
                sprite("green" + suffix),
                sprite("yellow" + suffix),
                sprite("purple" + suffix),
                sprite("white" + suffix)
        };
    }

    private static Identifier[] createOverlaySprites(String suffix) {
        return new Identifier[] {
                sprite("notched_6" + suffix),
                sprite("notched_10" + suffix),
                sprite("notched_12" + suffix),
                sprite("notched_20" + suffix)
        };
    }

    private static Identifier sprite(String name) {
        return Identifier.withDefaultNamespace("boss_bar/" + name);
    }
}
