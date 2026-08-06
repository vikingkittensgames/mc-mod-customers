package com.vikingkittens.mc.customers.client.compatability;

import org.joml.Matrix3x2fStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class GuiGraphicsCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void blitsTextureRegion() {
        GuiGraphics graphics = mock(GuiGraphics.class);
        TextureC texture = new TextureC(
                "customers",
                "textures/gui/receipt.png"
        );

        GuiGraphicsCUtils.blit(
                graphics,
                texture,
                1,
                2,
                3.0F,
                4.0F,
                5,
                6,
                7,
                8
        );

        verify(graphics).blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(
                        texture.namespace(),
                        texture.path()
                ),
                1,
                2,
                3.0F,
                4.0F,
                5,
                6,
                7,
                8
        );
    }
    @Test
    void appliesGuiTransforms() {
        GuiGraphics graphics = mock(GuiGraphics.class);
        Matrix3x2fStack pose = mock(Matrix3x2fStack.class);
        when(graphics.pose()).thenReturn(pose);

        GuiGraphicsCUtils.pushTransform(graphics);
        GuiGraphicsCUtils.translate(graphics, 3.0F, 4.0F);
        GuiGraphicsCUtils.scale(graphics, 1.5F, 2.0F);
        GuiGraphicsCUtils.popTransform(graphics);

        verify(pose).pushMatrix();
        verify(pose).translate(3.0F, 4.0F);
        verify(pose).scale(1.5F, 2.0F);
        verify(pose).popMatrix();
    }
    @Test
    void rendersScaledItem() {
        GuiGraphics graphics = mock(GuiGraphics.class);
        Matrix3x2fStack pose = mock(Matrix3x2fStack.class);
        ItemStack stack = mock(ItemStack.class);
        when(graphics.pose()).thenReturn(pose);

        GuiGraphicsCUtils.renderItem(graphics, stack, 4, 5, 0.5F);

        verify(pose).pushMatrix();
        verify(pose).translate(4.0F, 5.0F);
        verify(pose).scale(0.5F, 0.5F);
        verify(graphics).renderItem(stack, 0, 0);
        verify(pose).popMatrix();
    }
}
