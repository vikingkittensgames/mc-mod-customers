package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    /**
     * Renders customer request groups for customer spawner boss bars.
     *
     * @param event boss bar rendering event
     */
    @SubscribeEvent
    public static void onBossEventProgress(
            CustomizeGuiOverlayEvent.BossEventProgress event
    ) {
        CustomerSpawnerSnapshotManager.findByBossEvent(
                event.getBossEvent().getId()
        ).ifPresent(snapshot -> {
            event.setCanceled(true);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null
                    || !CustomerBossBarRenderer.isInRange(
                            minecraft.player,
                            snapshot.spawnerPos()
                    )) {
                return;
            }
            event.setIncrement(CustomerBossBarRenderer.render(
                    event.getGuiGraphics(),
                    event.getBossEvent(),
                    snapshot,
                    event.getX(),
                    event.getY(),
                    event.getIncrement()
            ));
        });
    }
}
