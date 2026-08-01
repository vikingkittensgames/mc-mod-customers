package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public class CustomerVillagerEntityRenderer extends
        MobRenderer<CustomerVillagerEntity, CustomerVillagerEntityRenderer.Model> {
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "customer_villager"
            ),
            "main"
    );
    private static final ResourceLocation VILLAGER_BASE_SKIN =
            ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");

    public CustomerVillagerEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(MODEL_LAYER)), 0.5F);
        addLayer(new CustomHeadLayer<>(
                this,
                context.getModelSet(),
                context.getItemInHandRenderer()
        ));
        addLayer(new VillagerProfessionLayer<>(
                this,
                context.getResourceManager(),
                "villager"
        ));
        addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerVillagerEntity entity) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected void scale(
            CustomerVillagerEntity entity,
            PoseStack poseStack,
            float partialTick
    ) {
        float scale = 0.9375F * entity.getAgeScale();
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected float getShadowRadius(CustomerVillagerEntity entity) {
        float shadowRadius = super.getShadowRadius(entity);
        return entity.isBaby() ? shadowRadius * 0.5F : shadowRadius;
    }

    public static class Model extends VillagerModel<CustomerVillagerEntity> {
        private final ModelPart rightLeg;
        private final ModelPart leftLeg;
        private final ModelPart jacket;
        private final ModelPart upperJacket;
        private final ModelPart lowerJacket;

        public Model(ModelPart root) {
            super(root);
            rightLeg = root.getChild("right_leg");
            leftLeg = root.getChild("left_leg");
            ModelPart body = root.getChild("body");
            jacket = body.getChild("jacket");
            upperJacket = body.getChild("upper_jacket");
            lowerJacket = body.getChild("lower_jacket");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = VillagerModel.createBodyModel();
            PartDefinition body = mesh.getRoot().getChild("body");
            body.addOrReplaceChild(
                    "upper_jacket",
                    CubeListBuilder.create()
                            .texOffs(0, 38)
                            .addBox(
                                    -4.0F,
                                    0.0F,
                                    -3.0F,
                                    8.0F,
                                    12.0F,
                                    6.0F,
                                    new CubeDeformation(0.5F)
                            ),
                    PartPose.ZERO
            );
            body.addOrReplaceChild(
                    "lower_jacket",
                    CubeListBuilder.create()
                            .texOffs(0, 49)
                            .addBox(
                                    -4.0F,
                                    -2.0F,
                                    -3.0F,
                                    8.0F,
                                    9.0F,
                                    6.0F,
                                    new CubeDeformation(0.6F, 0.5F, 0.6F)
                            ),
                    PartPose.offset(0.0F, 12.0F, 0.0F)
            );
            return LayerDefinition.create(mesh, 64, 64);
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
            if (riding) {
                applySittingLegPose(rightLeg, leftLeg);
            }
            applyJacketPose(
                    riding,
                    jacket,
                    upperJacket,
                    lowerJacket,
                    rightLeg.xRot
            );
        }

        static void applySittingLegPose(ModelPart rightLeg, ModelPart leftLeg) {
            rightLeg.xRot = -1.4137167F;
            rightLeg.yRot = (float) (Math.PI / 10.0D);
            rightLeg.zRot = 0.07853982F;
            leftLeg.xRot = -1.4137167F;
            leftLeg.yRot = (float) (-Math.PI / 10.0D);
            leftLeg.zRot = -0.07853982F;
        }

        static void applyJacketPose(
                boolean riding,
                ModelPart jacket,
                ModelPart upperJacket,
                ModelPart lowerJacket,
                float legXRotation
        ) {
            jacket.visible = !riding;
            upperJacket.visible = riding;
            lowerJacket.visible = riding;
            lowerJacket.xRot = riding ? legXRotation : 0.0F;
        }
    }
}
