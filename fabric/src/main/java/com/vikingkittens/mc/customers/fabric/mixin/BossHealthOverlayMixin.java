package com.vikingkittens.mc.customers.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

import com.vikingkittens.mc.customers.client.customer.CustomerBossBarRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomerSpawnerSnapshotManager;

@Mixin(BossHealthOverlay.class)
abstract class BossHealthOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int customers$nextIncrement = 19;

    @Unique
    private boolean customers$skipTitle;

    @Shadow
    private void drawBar(
            GuiGraphics graphics,
            int x,
            int y,
            BossEvent bossEvent
    ) {}

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar("
                            + "Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V"
            )
    )
    private void renderCustomerBossBar(
            BossHealthOverlay overlay,
            GuiGraphics graphics,
            int x,
            int y,
            BossEvent bossEvent
    ) {
        CustomerSpawnerSnapshotManager.findByBossEvent(bossEvent.getId()).ifPresentOrElse(snapshot -> {
            customers$skipTitle = true;
            if (minecraft.player != null
                    && CustomerBossBarRenderer.isInRange(
                            minecraft.player,
                            snapshot.spawnerPos()
                    )) {
                customers$nextIncrement = CustomerBossBarRenderer.render(
                        graphics,
                        bossEvent,
                        snapshot,
                        x,
                        y,
                        customers$nextIncrement
                );
            }
        }, () -> drawBar(graphics, x, y, bossEvent));
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString("
                            + "Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
            )
    )
    private int renderBossBarTitle(
            GuiGraphics graphics,
            Font font,
            Component title,
            int x,
            int y,
            int color
    ) {
        if (customers$skipTitle) {
            customers$skipTitle = false;
            return 0;
        }
        return graphics.drawString(font, title, x, y, color);
    }

    @ModifyVariable(
            method = "render",
            at = @At("STORE"),
            index = 3,
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString("
                                    + "Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
                    )
            )
    )
    private int adjustCustomerBossBarIncrement(int vanillaIncrement) {
        int increment = customers$nextIncrement;
        customers$nextIncrement = vanillaIncrement;
        return increment;
    }
}
