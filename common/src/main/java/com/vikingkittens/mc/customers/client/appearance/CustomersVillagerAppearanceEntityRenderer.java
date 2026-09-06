package com.vikingkittens.mc.customers.client.appearance;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.client.compatability.EntityRendererCUtils;

public final class CustomersVillagerAppearanceEntityRenderer<
                T extends Mob & CustomersVillager>
        extends EntityRenderer<T, CustomersVillagerAppearanceEntityRenderer.RenderState> {
    private final MobRenderer<?, ?, ?> fallbackRenderer;

    public CustomersVillagerAppearanceEntityRenderer(
            EntityRendererProvider.Context context,
            MobRenderer<?, ?, ?> fallbackRenderer
    ) {
        super(context);
        CustomersVillagerClientAppearances.initialize(context);
        this.fallbackRenderer = fallbackRenderer;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            T entity,
            RenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        CustomersVillagerClientAppearance appearance =
                CustomersVillagerClientAppearances.get(entity);
        renderState.renderer = appearance == null ? fallbackRenderer : appearance.getRenderer(entity);
        renderState.delegateRenderState = EntityRendererCUtils.createRenderState(
                renderState.renderer,
                entity,
                partialTick
        );
        renderState.sittingOffset = entity.isVillagerSitting() && appearance != null
                ? appearance.getSittingOffset(entity)
                : Vec3.ZERO;
    }

    @Override
    public void submit(
            RenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        poseStack.translate(
                renderState.sittingOffset.x,
                renderState.sittingOffset.y,
                renderState.sittingOffset.z
        );
        try {
            EntityRendererCUtils.submit(
                    renderState.renderer,
                    renderState.delegateRenderState,
                    poseStack,
                    nodeCollector,
                    cameraRenderState
            );
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    protected float getShadowRadius(RenderState renderState) {
        return 0.5F;
    }

    public static final class RenderState extends EntityRenderState {
        private EntityRenderer<?, ?> renderer;
        private EntityRenderState delegateRenderState;
        private Vec3 sittingOffset = Vec3.ZERO;
    }
}
