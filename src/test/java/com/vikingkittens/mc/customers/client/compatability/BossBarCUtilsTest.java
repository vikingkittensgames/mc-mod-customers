package com.vikingkittens.mc.customers.client.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies vanilla-style boss-bar rendering through the compatibility API. */
class BossBarCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void rendersBackgroundAndProgressAtRequestedPosition() {
        GuiGraphics graphics = mock(GuiGraphics.class);
        BossEvent bossEvent = mock(BossEvent.class);
        when(bossEvent.getColor()).thenReturn(BossEvent.BossBarColor.RED);
        when(bossEvent.getOverlay()).thenReturn(BossEvent.BossBarOverlay.PROGRESS);
        when(bossEvent.getProgress()).thenReturn(0.5F);

        BossBarCUtils.render(graphics, 10, 20, bossEvent);

        verify(graphics).blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Identifier.withDefaultNamespace("boss_bar/red_background"),
                182,
                5,
                0,
                0,
                10,
                20,
                182,
                5
        );
        verify(graphics).blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Identifier.withDefaultNamespace("boss_bar/red_progress"),
                182,
                5,
                0,
                0,
                10,
                20,
                91,
                5
        );
    }
}
