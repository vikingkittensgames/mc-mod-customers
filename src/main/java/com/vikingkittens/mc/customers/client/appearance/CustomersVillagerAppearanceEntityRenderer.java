package com.vikingkittens.mc.customers.client.appearance;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

public final class CustomersVillagerAppearanceEntityRenderer<
                T extends Mob & CustomersVillager>
        extends EntityRenderer<T> {
    private final MobRenderer<?, ?> fallbackRenderer;

    public CustomersVillagerAppearanceEntityRenderer(
            EntityRendererProvider.Context context,
            MobRenderer<?, ?> fallbackRenderer
    ) {
        super(context);
        CustomersVillagerClientAppearances.initialize(context);
        this.fallbackRenderer = fallbackRenderer;
        shadowRadius = 0.5F;
    }

    @Override
    public void render(
            T entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        CustomersVillagerClientAppearance appearance =
                CustomersVillagerClientAppearances.get(entity);
        Vec3 sittingOffset = entity.isSitting() && appearance != null
                ? appearance.getSittingOffset(entity)
                : Vec3.ZERO;
        poseStack.pushPose();
        poseStack.translate(
                sittingOffset.x,
                sittingOffset.y,
                sittingOffset.z
        );
        try {
            renderer(entity, appearance).render(
                    entity,
                    entityYaw,
                    partialTick,
                    poseStack,
                    buffer,
                    packedLight
            );
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return renderer(entity).getTextureLocation(entity);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private MobRenderer<T, ?> renderer(T entity) {
        CustomersVillagerClientAppearance appearance =
                CustomersVillagerClientAppearances.get(entity);
        return renderer(entity, appearance);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private MobRenderer<T, ?> renderer(
            T entity,
            CustomersVillagerClientAppearance appearance
    ) {
        MobRenderer renderer = appearance == null
                ? fallbackRenderer
                : appearance.getRenderer(entity);
        return renderer;
    }
}
