package com.vikingkittens.mc.customers.client.customer.special;

import com.vikingkittens.mc.customers.customer.special.CustomerStrayEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class CustomerStrayEntityRenderer extends HumanoidMobRenderer<CustomerStrayEntity, HumanoidModel<CustomerStrayEntity>> {
    private static final ResourceLocation STRAY_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/stray.png");
    private static final ResourceLocation STRAY_OVERLAY_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png");

    public CustomerStrayEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.STRAY)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.STRAY_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.STRAY_OUTER_ARMOR)),
                context.getModelManager()
        ));
        this.addLayer(new CustomerHumanoidOverlayLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.STRAY_OUTER_LAYER)),
                STRAY_OVERLAY_LOCATION
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerStrayEntity entity) {
        return STRAY_LOCATION;
    }
}
