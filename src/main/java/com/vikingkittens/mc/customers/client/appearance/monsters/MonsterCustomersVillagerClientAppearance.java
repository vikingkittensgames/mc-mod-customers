package com.vikingkittens.mc.customers.client.appearance.monsters;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerVariation;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearance;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public final class MonsterCustomersVillagerClientAppearance
        implements CustomersVillagerClientAppearance {
    private final MobRenderer<?, ?> zombie;
    private final MobRenderer<?, ?> skeleton;
    private final MobRenderer<?, ?> witch;
    private final MobRenderer<?, ?> husk;
    private final MobRenderer<?, ?> drowned;
    private final MobRenderer<?, ?> stray;

    public MonsterCustomersVillagerClientAppearance(
            EntityRendererProvider.Context context
    ) {
        zombie = humanoid(
                context,
                ModelLayers.ZOMBIE,
                ModelLayers.ZOMBIE_INNER_ARMOR,
                ModelLayers.ZOMBIE_OUTER_ARMOR,
                "textures/entity/zombie/zombie.png",
                true,
                1.0F
        );
        skeleton = humanoid(
                context,
                ModelLayers.SKELETON,
                ModelLayers.SKELETON_INNER_ARMOR,
                ModelLayers.SKELETON_OUTER_ARMOR,
                "textures/entity/skeleton/skeleton.png",
                false,
                1.0F
        );
        witch = new MonsterCustomersVillagerWitchRenderer(context);
        husk = humanoid(
                context,
                ModelLayers.HUSK,
                ModelLayers.HUSK_INNER_ARMOR,
                ModelLayers.HUSK_OUTER_ARMOR,
                "textures/entity/zombie/husk.png",
                true,
                1.0625F
        );

        MonsterCustomersVillagerHumanoidRenderer drownedRenderer =
                humanoid(
                        context,
                        ModelLayers.DROWNED,
                        ModelLayers.DROWNED_INNER_ARMOR,
                        ModelLayers.DROWNED_OUTER_ARMOR,
                        "textures/entity/zombie/drowned.png",
                        true,
                        1.0F
        );
        drownedRenderer.addOverlay(
                new MonsterCustomersVillagerHumanoidRenderer.Model(
                        context.bakeLayer(
                                ModelLayers.DROWNED_OUTER_LAYER
                        ),
                        true
                ),
                ResourceLocation.withDefaultNamespace(
                        "textures/entity/zombie/drowned_outer_layer.png"
                )
        );
        drowned = drownedRenderer;

        MonsterCustomersVillagerHumanoidRenderer strayRenderer =
                humanoid(
                        context,
                        ModelLayers.STRAY,
                        ModelLayers.STRAY_INNER_ARMOR,
                        ModelLayers.STRAY_OUTER_ARMOR,
                        "textures/entity/skeleton/stray.png",
                        false,
                        1.0F
                );
        strayRenderer.addOverlay(
                new HumanoidModel<CustomerVillagerEntity>(
                        context.bakeLayer(ModelLayers.STRAY_OUTER_LAYER)
                ),
                ResourceLocation.withDefaultNamespace(
                        "textures/entity/skeleton/stray_overlay.png"
                )
        );
        stray = strayRenderer;
    }

    @Override
    public MobRenderer<?, ?> getRenderer(CustomersVillager villager) {
        return switch (variation(villager)) {
            case ZOMBIE -> zombie;
            case SKELETON -> skeleton;
            case WITCH -> witch;
            case HUSK -> husk;
            case DROWNED -> drowned;
            case STRAY -> stray;
        };
    }

    @Override
    public float getNameTagOffset(CustomersVillager villager) {
        return variation(villager)
                        == MonsterCustomersVillagerVariation.WITCH
                ? 0.40F
                : 0.0F;
    }

    private static MonsterCustomersVillagerHumanoidRenderer humanoid(
            EntityRendererProvider.Context context,
            ModelLayerLocation modelLayer,
            ModelLayerLocation innerArmor,
            ModelLayerLocation outerArmor,
            String texture,
            boolean zombieArms,
            float scale
    ) {
        return new MonsterCustomersVillagerHumanoidRenderer(
                context,
                modelLayer,
                innerArmor,
                outerArmor,
                ResourceLocation.withDefaultNamespace(texture),
                zombieArms,
                scale
        );
    }

    private static MonsterCustomersVillagerVariation variation(
            CustomersVillager villager
    ) {
        return MonsterCustomersVillagerVariation.fromSeed(
                villager.getVariationSeed()
        );
    }
}
