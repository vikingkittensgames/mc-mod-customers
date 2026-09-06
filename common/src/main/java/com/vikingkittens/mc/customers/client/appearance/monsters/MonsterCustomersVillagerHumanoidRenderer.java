package com.vikingkittens.mc.customers.client.appearance.monsters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

final class MonsterCustomersVillagerHumanoidRenderer<T extends Mob & CustomersVillager>
        extends HumanoidMobRenderer<
                T,
                ZombieRenderState,
                MonsterCustomersVillagerHumanoidRenderer.Model> {
    private final Identifier texture;
    private final float modelScale;

    MonsterCustomersVillagerHumanoidRenderer(
            EntityRendererProvider.Context context,
            ModelLayerLocation modelLayer,
            ArmorModelSet<ModelLayerLocation> armorLayers,
            Identifier texture,
            boolean zombieArms,
            float modelScale
    ) {
        super(context, new Model(context.bakeLayer(modelLayer), zombieArms), 0.5F);
        this.texture = texture;
        this.modelScale = modelScale;
        ArmorModelSet<HumanoidModel<ZombieRenderState>> armorModels = ArmorModelSet.bake(
                armorLayers,
                context.getModelSet(),
                HumanoidModel::new
        );
        addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
    }

    void addOverlay(HumanoidModel<ZombieRenderState> overlayModel, Identifier overlayTexture) {
        addLayer(new MonsterCustomersVillagerOverlayLayer(this, overlayModel, overlayTexture));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(
            T entity,
            ZombieRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.isAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState renderState) {
        return texture;
    }

    @Override
    protected void scale(ZombieRenderState renderState, PoseStack poseStack) {
        poseStack.scale(modelScale, modelScale, modelScale);
        super.scale(renderState, poseStack);
    }

    static final class Model extends HumanoidModel<ZombieRenderState> {
        private final boolean zombieArms;

        Model(ModelPart root, boolean zombieArms) {
            super(root);
            this.zombieArms = zombieArms;
        }

        @Override
        public void setupAnim(ZombieRenderState renderState) {
            super.setupAnim(renderState);
            if (zombieArms) {
                AnimationUtils.animateZombieArms(leftArm, rightArm, renderState.isAggressive, renderState);
            }
        }
    }
}
