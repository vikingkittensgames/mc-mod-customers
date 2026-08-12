package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
            GuiGraphics graphics,
            ItemStack stack,
            int x,
            int y,
            float scale
    ) {
        pushTransform(graphics);
        translate(graphics, x, y);
        scale(graphics, scale, scale);
        graphics.renderItem(stack, 0, 0);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, 0, 0);
        popTransform(graphics);
    }

    private static ResourceLocation toResourceLocation(TextureC texture) {
        return ResourceLocation.fromNamespaceAndPath(
                texture.namespace(),
                texture.path()
        );
    }
}
