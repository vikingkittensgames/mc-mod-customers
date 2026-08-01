package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Provides version-compatible GUI texture and transform operations.
 */
public final class GuiGraphicsCUtils {
    private GuiGraphicsCUtils() {
    }

    public static void blit(
            GuiGraphics graphics,
            TextureC texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        graphics.blit(
                toResourceLocation(texture),
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight
        );
    }

    public static void pushTransform(GuiGraphics graphics) {
        graphics.pose().pushPose();
    }

    public static void popTransform(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    public static void translate(
            GuiGraphics graphics,
            float x,
            float y
    ) {
        graphics.pose().translate(x, y, 0.0F);
    }

    public static void scale(
            GuiGraphics graphics,
            float x,
            float y
    ) {
        graphics.pose().scale(x, y, 1.0F);
    }

    private static ResourceLocation toResourceLocation(TextureC texture) {
        return ResourceLocation.fromNamespaceAndPath(
                texture.namespace(),
                texture.path()
        );
    }
}