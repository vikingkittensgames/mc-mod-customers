package com.vikingkittens.mc.customers.client.appearance.monsters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

final class MonsterCustomersVillagerOverlayLayer
        extends RenderLayer<ZombieRenderState, MonsterCustomersVillagerHumanoidRenderer.Model> {
    private final HumanoidModel<ZombieRenderState> overlayModel;
    private final Identifier texture;

    MonsterCustomersVillagerOverlayLayer(
            RenderLayerParent<ZombieRenderState, MonsterCustomersVillagerHumanoidRenderer.Model> renderer,
            HumanoidModel<ZombieRenderState> overlayModel,
            Identifier texture
    ) {
        super(renderer);
        this.overlayModel = overlayModel;
        this.texture = texture;
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            ZombieRenderState renderState,
            float yRot,
            float xRot
    ) {
        overlayModel.setupAnim(renderState);
        renderColoredCutoutModel(overlayModel, texture, poseStack, nodeCollector, packedLight, renderState, -1, 0);
    }
}
