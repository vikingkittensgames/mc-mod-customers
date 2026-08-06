package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Provides version-compatible GUI texture and transform operations.
 */
public final class GuiGraphicsCUtils {
    private GuiGraphicsCUtils() {
    }

    /**
     * Renders an item and its decorations at the requested GUI scale.
     *
     * @param graphics GUI drawing context
     * @param stack item stack to render
     * @param x left position before scaling
     * @param y top position before scaling
     * @param scale item scale relative to the standard 16-pixel icon
     */
    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, float scale) {
        pushTransform(graphics);
        translate(graphics, x, y);
        scale(graphics, scale, scale);
        graphics.renderItem(stack, 0, 0);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            graphics.renderItemDecorations(minecraft.font, stack, 0, 0);
        }
        popTransform(graphics);
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
