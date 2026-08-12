package com.vikingkittens.mc.customers.client.customer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public final class CustomerCounterMarkerNeoForgeEvents {
    private CustomerCounterMarkerNeoForgeEvents() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            CustomerCounterMarkerRenderer.render(event.getPoseStack(), event.getCamera().getPosition());
        }
    }
}
