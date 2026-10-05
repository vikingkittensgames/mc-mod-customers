package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerHeadTracking;

final class SkinCustomersVillagerGeckoModel extends GeoModel<SkinCustomersVillagerGeckoProxy> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ResourceLocation> MISSING_HEAD_BONES = new HashSet<>();

    @Override
    public ResourceLocation getModelResource(SkinCustomersVillagerGeckoProxy animatable) {
        return SkinCustomersVillagerGeckoResources.getModelResource(animatable.getDefinition());
    }

    @Override
    public ResourceLocation getTextureResource(SkinCustomersVillagerGeckoProxy animatable) {
        return animatable.getDefinition().getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SkinCustomersVillagerGeckoProxy animatable) {
        return SkinCustomersVillagerGeckoResources.getAnimationResource(animatable.getDefinition());
    }

    @Override
    public void setCustomAnimations(
            SkinCustomersVillagerGeckoProxy animatable,
            long instanceId,
            AnimationState<SkinCustomersVillagerGeckoProxy> animationState
    ) {
        SkinCustomersVillagerDefinition definition = animatable.getDefinition();
        GeoBone head = getBone(definition.getHeadBone()).orElse(null);
        if (head == null) {
            ResourceLocation model = SkinCustomersVillagerGeckoResources.getModelResource(definition);
            if (MISSING_HEAD_BONES.add(model)) {
                LOGGER.warn("Gecko skin model {} does not contain head bone '{}'", model, definition.getHeadBone());
            }
            return;
        }
        head.setTrackingMatrices(true);
        SkinCustomersVillagerHeadTracking tracking = definition.getHeadTracking();
        EntityModelData modelData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (tracking.enabled() && modelData != null) {
            SkinCustomersVillagerGeckoHeadRotation rotation = SkinCustomersVillagerGeckoHeadRotation.create(
                    head.getRotX(),
                    head.getRotY(),
                    head.getInitialSnapshot().getRotX(),
                    head.getInitialSnapshot().getRotY(),
                    head.hasRotationChanged(),
                    SkinCustomersVillagerGeckoGeometry.trackingRotation(
                            modelData.headPitch(),
                            tracking.pitchMultiplier(),
                            tracking.maximumPitch()
                    ),
                    SkinCustomersVillagerGeckoGeometry.trackingRotation(
                            modelData.netHeadYaw(),
                            tracking.yawMultiplier(),
                            tracking.maximumYaw()
                    )
            );
            head.setRotX(rotation.renderedPitch());
            head.setRotY(rotation.renderedYaw());
        }
    }
}
