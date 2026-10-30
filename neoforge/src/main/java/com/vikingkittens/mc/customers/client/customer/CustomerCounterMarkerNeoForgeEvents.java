package com.vikingkittens.mc.customers.client.customer;

import org.joml.Matrix4f;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public final class CustomerCounterMarkerNeoForgeEvents {
    private CustomerCounterMarkerNeoForgeEvents() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        CustomerCounterMarkerRenderer.render(
                event.getPoseStack(),
                event.getLevelRenderState().cameraRenderState,
                new Matrix4f(event.getModelViewMatrix())
        );
    }
}
