package com.vikingkittens.mc.customers.client.appearance.monsters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

final class MonsterCustomersVillagerHumanoidRenderer
        extends HumanoidMobRenderer<
                CustomerVillagerEntity,
                MonsterCustomersVillagerHumanoidRenderer.Model> {
    private final ResourceLocation texture;
    private final float modelScale;

    MonsterCustomersVillagerHumanoidRenderer(
            EntityRendererProvider.Context context,
            ModelLayerLocation modelLayer,
            ModelLayerLocation innerArmorLayer,
            ModelLayerLocation outerArmorLayer,
            ResourceLocation texture,
            boolean zombieArms,
            float modelScale
    ) {
        super(
                context,
                new Model(
                        context.bakeLayer(modelLayer),
                        zombieArms
                ),
                0.5F
        );
        this.texture = texture;
        this.modelScale = modelScale;
        addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(
                        context.bakeLayer(innerArmorLayer)
                ),
                new HumanoidModel<>(
                        context.bakeLayer(outerArmorLayer)
                ),
                context.getModelManager()
        ));
    }

    void addOverlay(
            HumanoidModel<CustomerVillagerEntity> overlayModel,
            ResourceLocation overlayTexture
    ) {
        addLayer(new MonsterCustomersVillagerOverlayLayer(
                this,
                overlayModel,
                overlayTexture
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(
            CustomerVillagerEntity entity
    ) {
        return texture;
    }

    @Override
    protected void scale(
            CustomerVillagerEntity entity,
            PoseStack poseStack,
            float partialTick
    ) {
        poseStack.scale(modelScale, modelScale, modelScale);
        super.scale(entity, poseStack, partialTick);
    }

    static final class Model
            extends HumanoidModel<CustomerVillagerEntity> {
        private final boolean zombieArms;

        Model(ModelPart root, boolean zombieArms) {
            super(root);
            this.zombieArms = zombieArms;
        }

        @Override
        public void setupAnim(
                CustomerVillagerEntity entity,
                float limbSwing,
                float limbSwingAmount,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            super.setupAnim(
                    entity,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    netHeadYaw,
                    headPitch
            );
            if (zombieArms) {
                AnimationUtils.animateZombieArms(
                        leftArm,
                        rightArm,
                        entity.isAggressive(),
                        attackTime,
                        ageInTicks
                );
            }
        }
    }
}
