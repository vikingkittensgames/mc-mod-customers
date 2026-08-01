package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

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
                RenderPipelines.GUI_TEXTURED,
                toIdentifier(texture),
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
        graphics.pose().pushMatrix();
    }
    public static void popTransform(GuiGraphics graphics) {
        graphics.pose().popMatrix();
    }
    public static void translate(
            GuiGraphics graphics,
            float x,
            float y
    ) {
        graphics.pose().translate(x, y);
    }
    public static void scale(
            GuiGraphics graphics,
            float x,
            float y
    ) {
        graphics.pose().scale(x, y);
    }
    private static Identifier toIdentifier(TextureC texture) {
        return Identifier.fromNamespaceAndPath(
                texture.namespace(),
                texture.path()
        );
    }
}
