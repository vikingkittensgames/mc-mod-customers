package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import org.joml.Vector3d;
import org.slf4j.Logger;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerGeckoEntityRenderer<T extends Mob & CustomersVillager> extends EntityRenderer<T>
        implements SkinCustomersVillagerGeckoRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SkinCustomersVillagerRenderer<T> fallbackRenderer;
    private final Delegate delegate;
    private final Map<T, CachedProxy> proxies = new WeakHashMap<>();
    private final Set<ResourceLocation> failedModels = new HashSet<>();

    SkinCustomersVillagerGeckoEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        fallbackRenderer = new SkinCustomersVillagerRenderer<>(context, false);
        delegate = new Delegate(context);
    }

    @Override
    public EntityRenderer<?> getRenderer(CustomersVillager villager) {
        return this;
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
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerClientAppearance.getSkin(entity);
        SkinCustomersVillagerGeckoProxy proxy = proxy(entity, definition);
        PoseStack.Pose callerPose = poseStack.last();
        poseStack.pushPose();
        try {
            delegate.render(proxy, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        } catch (RuntimeException exception) {
            ResourceLocation model = SkinCustomersVillagerGeckoResources.getModelResource(definition);
            if (failedModels.add(model)) {
                LOGGER.error("Unable to render Gecko skin model {}; using the wide skin renderer", model, exception);
            }
        } finally {
            SkinCustomersVillagerGeckoRenderState.restorePoseStack(poseStack, callerPose);
        }
        fallbackRenderer.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return SkinCustomersVillagerClientAppearance.getSkin(entity).getTextureLocation();
    }

    @Override
    public Vec3 getSittingOffset(CustomersVillager villager) {
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerClientAppearance.getSkin(villager);
        if (!definition.model().isGecko() || definition.sittingPivot().isEmpty() || !(villager instanceof Mob entity)) {
            return Vec3.ZERO;
        }
        return SkinCustomersVillagerGeckoGeometry.sittingOffset(
                definition.sittingPivot().orElseThrow(),
                definition.scale(),
                entity.yBodyRot
        );
    }

    @Override
    public Optional<Vec3> getOverheadAnchor(CustomersVillager villager) {
        if (!(villager instanceof Mob entity)) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        T typedEntity = (T) entity;
        CachedProxy cached = proxies.get(typedEntity);
        return cached == null ? Optional.empty() : Optional.ofNullable(cached.proxy().getOverheadAnchor());
    }

    private SkinCustomersVillagerGeckoProxy proxy(T entity, SkinCustomersVillagerDefinition definition) {
        CachedProxy cached = proxies.get(entity);
        if (cached == null || !cached.definition().equals(definition)) {
            cached = new CachedProxy(definition, new SkinCustomersVillagerGeckoProxy(entity.level(), definition));
            proxies.put(entity, cached);
        }
        cached.proxy().syncFrom(entity, entity);
        return cached.proxy();
    }

    private record CachedProxy(
            SkinCustomersVillagerDefinition definition,
            SkinCustomersVillagerGeckoProxy proxy
    ) {}

    private static final class Delegate extends GeoEntityRenderer<SkinCustomersVillagerGeckoProxy> {
        private Delegate(EntityRendererProvider.Context context) {
            super(context, new SkinCustomersVillagerGeckoModel());
        }

        @Override
        public void preRender(
                PoseStack poseStack,
                SkinCustomersVillagerGeckoProxy animatable,
                BakedGeoModel model,
                MultiBufferSource bufferSource,
                VertexConsumer buffer,
                boolean isReRender,
                float partialTick,
                int packedLight,
                int packedOverlay,
                int colour
        ) {
            super.preRender(
                    poseStack,
                    animatable,
                    model,
                    bufferSource,
                    buffer,
                    isReRender,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    colour
            );
            float scale = animatable.getDefinition().scale();
            poseStack.scale(scale, scale, scale);
        }

        @Override
        public void renderRecursively(
                PoseStack poseStack,
                SkinCustomersVillagerGeckoProxy animatable,
                GeoBone bone,
                RenderType renderType,
                MultiBufferSource bufferSource,
                VertexConsumer buffer,
                boolean isReRender,
                float partialTick,
                int packedLight,
                int packedOverlay,
                int colour
        ) {
            super.renderRecursively(
                    poseStack,
                    animatable,
                    bone,
                    renderType,
                    bufferSource,
                    buffer,
                    isReRender,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    colour
            );
            if (bone.getName().equals(animatable.getDefinition().getHeadBone()) && bone.isTrackingMatrices()) {
                Vector3d position = bone.getLocalPosition();
                animatable.setOverheadAnchor(new Vec3(position.x, position.y, position.z));
            }
        }

        @Override
        protected float getShadowRadius(SkinCustomersVillagerGeckoProxy entity) {
            return entity.getDefinition().shadowRadius();
        }
    }
}
