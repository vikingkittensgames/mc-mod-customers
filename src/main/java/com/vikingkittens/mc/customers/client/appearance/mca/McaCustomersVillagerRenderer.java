package com.vikingkittens.mc.customers.client.appearance.mca;

import java.util.Map;
import java.util.WeakHashMap;

import net.conczin.mca.client.render.VillagerEntityMCARenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.mca.McaCustomersVillagerVariation;

final class McaCustomersVillagerRenderer<
                T extends Mob & CustomersVillager>
        extends MobRenderer<T, PlayerModel<T>> {
    private final VillagerEntityMCARenderer delegate;
    private final Map<T, CachedProxy> proxies = new WeakHashMap<>();

    McaCustomersVillagerRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new PlayerModel<>(
                        context.bakeLayer(ModelLayers.PLAYER),
                        false
                ),
                0.5F
        );
        delegate = new VillagerEntityMCARenderer(context);
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
        delegate.render(
                proxy(entity),
                entityYaw,
                partialTick,
                poseStack,
                buffer,
                packedLight
        );
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return delegate.getTextureLocation(proxy(entity));
    }

    Vec3 getSittingOffset(CustomersVillager villager) {
        if (!(villager instanceof Mob entity)) {
            return Vec3.ZERO;
        }
        @SuppressWarnings("unchecked")
        T typedEntity = (T) entity;
        return McaCustomersVillagerProxyState.sittingOffset(
                proxy(typedEntity).getRawVerticalScaleFactor()
        );
    }

    private McaCustomersVillagerProxy proxy(T entity) {
        float variationSeed = entity.getVariationSeed();
        CachedProxy cached = proxies.get(entity);
        if (cached == null
                || Float.compare(cached.variationSeed(), variationSeed)
                        != 0) {
            cached = new CachedProxy(
                    variationSeed,
                    new McaCustomersVillagerProxy(
                            entity.level(),
                            McaCustomersVillagerVariation.fromSeed(
                                    variationSeed
                            )
                    )
            );
            proxies.put(entity, cached);
        }
        cached.proxy().syncFrom(entity, entity);
        return cached.proxy();
    }

    private record CachedProxy(
            float variationSeed,
            McaCustomersVillagerProxy proxy
    ) {}
}
