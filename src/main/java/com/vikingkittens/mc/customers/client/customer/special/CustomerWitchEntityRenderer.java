package com.vikingkittens.mc.customers.client.customer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vikingkittens.mc.customers.client.customer.CustomerClientEvents;
import com.vikingkittens.mc.customers.client.customer.CustomerVillagerEntityRenderer;
import com.vikingkittens.mc.customers.customer.special.CustomerWitchEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CustomerWitchEntityRenderer extends MobRenderer<
        CustomerWitchEntity,
        WitchRenderState,
        WitchModel
> {
    private static final Identifier WITCH_LOCATION = Identifier.withDefaultNamespace("textures/entity/witch.png");

    public CustomerWitchEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
        this.addLayer(new WitchItemLayer(this));
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState renderState) {
        return WITCH_LOCATION;
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public void extractRenderState(
            CustomerWitchEntity entity,
            WitchRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        HoldingEntityRenderState.extractHoldingEntityRenderState(
                entity,
                renderState,
                this.itemModelResolver
        );
        renderState.entityId = entity.getId();
        ItemStack itemStack = entity.getMainHandItem();
        renderState.isHoldingItem = !itemStack.isEmpty();
        renderState.isHoldingPotion = itemStack.is(Items.POTION);
    }

    @Override
    protected void scale(WitchRenderState renderState, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    /**
     * Applies the customer sitting pose to the standard witch model.
     */
    public static class Model extends WitchModel {
        private final ModelPart rightLeg;
        private final ModelPart leftLeg;

        /**
         * Creates a witch model backed by the baked model root.
         */
        public Model(ModelPart root) {
            super(root);
            rightLeg = root.getChild("right_leg");
            leftLeg = root.getChild("left_leg");
        }

        /**
         * Applies standard witch animation and the customer sitting pose.
         */
        @Override
        public void setupAnim(WitchRenderState renderState) {
            super.setupAnim(renderState);
            if (Boolean.TRUE.equals(
                    renderState.getRenderData(
                            CustomerClientEvents.CUSTOMER_SITTING_RENDER_DATA
                    )
            )) {
                CustomerVillagerEntityRenderer.Model.applySittingLegPose(
                        rightLeg,
                        leftLeg
                );
            }
        }
    }
}