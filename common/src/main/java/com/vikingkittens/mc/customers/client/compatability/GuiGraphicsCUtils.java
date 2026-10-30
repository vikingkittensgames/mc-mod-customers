package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Provides version-compatible GUI texture and transform operations.
 */
public final class GuiGraphicsCUtils {
    private GuiGraphicsCUtils() {
    }

    public static void blit(
            GuiGraphicsExtractor graphics,
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

    public static void blit(
            GuiGraphicsExtractor graphics,
            Identifier texture,
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
                texture,
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

    public static void pushTransform(GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
    }

    public static void popTransform(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }

    public static void translate(
            GuiGraphicsExtractor graphics,
            float x,
            float y
    ) {
        graphics.pose().translate(x, y);
    }

    public static void scale(
            GuiGraphicsExtractor graphics,
            float x,
            float y
    ) {
        graphics.pose().scale(x, y);
    }

    /**
     * Renders an item and its count at a scaled GUI position.
     *
     * @param graphics GUI rendering context
     * @param stack item stack to render
     * @param x left coordinate
     * @param y top coordinate
     * @param scale item rendering scale
     */
    public static void renderItem(
            GuiGraphicsExtractor graphics,
            ItemStack stack,
            int x,
            int y,
            float scale
    ) {
        pushTransform(graphics);
        translate(graphics, x, y);
        scale(graphics, scale, scale);
        graphics.item(stack, 0, 0);
        graphics.itemDecorations(Minecraft.getInstance().font, stack, 0, 0);
        popTransform(graphics);
    }

    private static Identifier toIdentifier(TextureC texture) {
        return Identifier.fromNamespaceAndPath(
                texture.namespace(),
                texture.path()
        );
    }
}
