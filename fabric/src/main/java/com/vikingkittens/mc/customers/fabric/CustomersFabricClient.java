package com.vikingkittens.mc.customers.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

import com.vikingkittens.mc.customers.client.common.SpawnerIconItemRenderer;
import com.vikingkittens.mc.customers.client.CustomersClientRegistrations;
import com.vikingkittens.mc.customers.client.advancements.ftb.CustomersFTBClient;
import com.vikingkittens.mc.customers.client.customer.CustomerCounterMarkerRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomersClientNetworking;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CustomersClientNetworking.initialize();
        CustomersClientRegistrations.initialize();
        BuiltinItemRendererRegistry.INSTANCE.register(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get(),
                (stack, mode, pose, buffers, light, overlay) ->
                        SpawnerIconItemRenderer.render(stack, pose, buffers, light, overlay));
        BuiltinItemRendererRegistry.INSTANCE.register(SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get(),
                (stack, mode, pose, buffers, light, overlay) ->
                        SpawnerIconItemRenderer.render(stack, pose, buffers, light, overlay));
        CustomersFTBClient.initialize();
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.matrixStack() != null) {
                CustomerCounterMarkerRenderer.render(
                        context.matrixStack(),
                        context.camera().getPosition()
                );
            }
        });
    }
}
