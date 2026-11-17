package com.vikingkittens.mc.customers.fabric;

import java.util.Map;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import com.vikingkittens.mc.customers.client.CustomersClientRegistrations;
import com.vikingkittens.mc.customers.client.advancements.ftb.CustomersFTBClient;
import com.vikingkittens.mc.customers.client.compatability.RenderCUtils;
import com.vikingkittens.mc.customers.client.customer.CustomerCounterMarkerRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomersClientNetworking;
import com.vikingkittens.mc.customers.client.customer.CustomersDisplayCaseGlassRenderer;
import com.vikingkittens.mc.customers.fabric.mixin.RenderPipelinesAccessor;
import com.vikingkittens.mc.customers.fabric.mixin.RenderTypeAccessor;

public final class CustomersFabricClient implements ClientModInitializer {
    private static final Map<
            Identifier,
            ExtraModelKey<BlockStateModel>
    > DISPLAY_CASE_GLASS_MODELS = CustomersDisplayCaseGlassRenderer
            .modelIds()
            .stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                    Function.identity(),
                    modelId -> ExtraModelKey.create(modelId::toString)
            ));

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(context ->
                DISPLAY_CASE_GLASS_MODELS.forEach(
                        (modelId, key) -> context.addModel(
                                key,
                                SimpleUnbakedExtraModel.blockStateModel(
                                        modelId
                                )
                        )
                ));
        CustomersDisplayCaseGlassRenderer.setModelProvider(modelId ->
                ((FabricBakedModelManager) Minecraft.getInstance()
                        .getModelManager())
                        .getModel(DISPLAY_CASE_GLASS_MODELS.get(modelId)));
        RenderPipelinesAccessor.customers$register(
                RenderCUtils.getTranslucentNoDepthPipeline()
        );
        RenderCUtils.setTranslucentNoDepthRenderType(
                RenderTypeAccessor.customers$create(
                        "customers_translucent_no_depth",
                        RenderCUtils.getTranslucentNoDepthRenderSetup()
                )
        );
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
