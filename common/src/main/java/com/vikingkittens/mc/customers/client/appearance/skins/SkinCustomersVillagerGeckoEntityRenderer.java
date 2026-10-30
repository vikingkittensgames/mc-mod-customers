package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import org.slf4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerHeadTracking;
import com.vikingkittens.mc.customers.client.compatability.EntityRendererCUtils;

final class SkinCustomersVillagerGeckoEntityRenderer<T extends Mob & CustomersVillager>
        extends EntityRenderer<T, SkinCustomersVillagerGeckoEntityRenderer.RenderState>
        implements SkinCustomersVillagerGeckoRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SkinCustomersVillagerRenderer<T> fallbackRenderer;
    private final Delegate delegate;
    private final Map<T, CachedProxy> proxies = new WeakHashMap<>();
    private final Set<Identifier> failedModels = new HashSet<>();

    SkinCustomersVillagerGeckoEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        fallbackRenderer = new SkinCustomersVillagerRenderer<>(context, false);
        delegate = new Delegate(context);
    }

    @Override
    public EntityRenderer<?, ?> getRenderer(CustomersVillager villager) {
        return this;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(T entity, RenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerClientAppearance.getSkin(entity);
        SkinCustomersVillagerGeckoProxy proxy = proxy(entity, definition);
        renderState.delegateRenderState = delegate.createRenderState(proxy, partialTick);
        renderState.fallbackRenderState = fallbackRenderer.createRenderState(entity, partialTick);
        renderState.nameTag = renderState.delegateRenderState.nameTag;
    }

    @Override
    public void submit(
            RenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            CameraRenderState cameraRenderState
    ) {
        try {
            delegate.submit(
                    renderState.delegateRenderState,
                    poseStack,
                    nodeCollector,
                    cameraRenderState
            );
        } catch (RuntimeException exception) {
            Identifier model = renderState.delegateRenderState.getGeckolibData(
                    SkinCustomersVillagerGeckoData.DEFINITION
            ).model().resource();
            if (failedModels.add(model)) {
                LOGGER.error("Unable to render Gecko skin model {}; using the wide skin renderer", model, exception);
            }
            EntityRendererCUtils.submit(
                    fallbackRenderer,
                    renderState.fallbackRenderState,
                    poseStack,
                    nodeCollector,
                    cameraRenderState
            );
        }
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

    static final class RenderState extends EntityRenderState {
        private SkinCustomersVillagerGeckoRenderState delegateRenderState;
        private EntityRenderState fallbackRenderState;
    }

    private record CachedProxy(
            SkinCustomersVillagerDefinition definition,
            SkinCustomersVillagerGeckoProxy proxy
    ) {}

    private static final class Delegate
            extends GeoEntityRenderer<SkinCustomersVillagerGeckoProxy, SkinCustomersVillagerGeckoRenderState> {
        private final Set<Identifier> missingHeadBones = new HashSet<>();

        private Delegate(EntityRendererProvider.Context context) {
            super(context, new SkinCustomersVillagerGeckoModel());
        }

        @Override
        public SkinCustomersVillagerGeckoRenderState createRenderState(
                SkinCustomersVillagerGeckoProxy animatable,
                Void relatedObject
        ) {
            return new SkinCustomersVillagerGeckoRenderState();
        }

        @Override
        public void addRenderData(
                SkinCustomersVillagerGeckoProxy animatable,
                Void relatedObject,
                SkinCustomersVillagerGeckoRenderState renderState,
                float partialTick
        ) {
            renderState.addGeckolibData(SkinCustomersVillagerGeckoData.DEFINITION, animatable.getDefinition());
            renderState.addGeckolibData(SkinCustomersVillagerGeckoData.PROXY, animatable);
        }

        @Override
        public void scaleModelForRender(
                RenderPassInfo<SkinCustomersVillagerGeckoRenderState> renderPassInfo,
                float widthScale,
                float heightScale
        ) {
            float scale = definition(renderPassInfo).scale();
            super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
        }

        @Override
        public void adjustModelBonesForRender(
                RenderPassInfo<SkinCustomersVillagerGeckoRenderState> renderPassInfo,
                BoneSnapshots snapshots
        ) {
            super.adjustModelBonesForRender(renderPassInfo, snapshots);
            SkinCustomersVillagerDefinition definition = definition(renderPassInfo);
            SkinCustomersVillagerHeadTracking tracking = definition.getHeadTracking();
            if (!tracking.enabled()) {
                return;
            }
            snapshots.get(definition.getHeadBone()).ifPresentOrElse(
                    head -> SkinCustomersVillagerGeckoHeadRotation.apply(
                            head,
                            SkinCustomersVillagerGeckoGeometry.trackingRotation(
                                    renderPassInfo.renderState().xRot,
                                    tracking.pitchMultiplier(),
                                    tracking.maximumPitch()
                            ),
                            SkinCustomersVillagerGeckoGeometry.trackingRotation(
                                    renderPassInfo.renderState().yRot,
                                    tracking.yawMultiplier(),
                                    tracking.maximumYaw()
                            )
                    ),
                    () -> logMissingHeadBone(definition)
            );
        }

        @Override
        public void preRenderPass(
                RenderPassInfo<SkinCustomersVillagerGeckoRenderState> renderPassInfo,
                SubmitNodeCollector nodeCollector
        ) {
            super.preRenderPass(renderPassInfo, nodeCollector);
            SkinCustomersVillagerDefinition definition = definition(renderPassInfo);
            SkinCustomersVillagerGeckoProxy proxy = renderPassInfo.getGeckolibData(
                    SkinCustomersVillagerGeckoData.PROXY
            );
            proxy.setOverheadAnchor(null);
            renderPassInfo.addBonePositionListener(
                    definition.getHeadBone(),
                    (worldPosition, modelPosition, localPosition) -> proxy.setOverheadAnchor(localPosition)
            );
        }

        private SkinCustomersVillagerDefinition definition(
                RenderPassInfo<SkinCustomersVillagerGeckoRenderState> renderPassInfo
        ) {
            return renderPassInfo.getGeckolibData(SkinCustomersVillagerGeckoData.DEFINITION);
        }

        private void logMissingHeadBone(SkinCustomersVillagerDefinition definition) {
            Identifier model = SkinCustomersVillagerGeckoResources.getModelResource(definition);
            if (missingHeadBones.add(model)) {
                LOGGER.warn("Gecko skin model {} does not contain head bone '{}'", model, definition.getHeadBone());
            }
        }
    }
}
