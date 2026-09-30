package com.vikingkittens.mc.customers.fabric;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import com.vikingkittens.mc.customers.client.CustomersClientRegistrations;
import com.vikingkittens.mc.customers.client.advancements.ftb.CustomersFTBClient;
import com.vikingkittens.mc.customers.client.customer.CustomerCounterMarkerRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomersClientNetworking;

public final class CustomersFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CustomersClientNetworking.initialize();
        CustomersClientRegistrations.initialize();
        CustomersFTBClient.initialize();
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.matrices() != null) {
                CustomerCounterMarkerRenderer.render(
                        context.matrices(),
                        context.worldState().cameraRenderState,
                        RenderSystem.getModelViewMatrix()
                );
            }
        });
    }
}
