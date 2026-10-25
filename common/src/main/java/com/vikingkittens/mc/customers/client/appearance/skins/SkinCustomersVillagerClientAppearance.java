package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.Optional;

import dev.architectury.platform.Platform;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerModel;
import com.vikingkittens.mc.customers.appearance.skins.SkinPackCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearance;

public final class SkinCustomersVillagerClientAppearance implements CustomersVillagerClientAppearance {
    private final MobRenderer<?, ?> wideRenderer;
    private final MobRenderer<?, ?> slimRenderer;
    private final Optional<SkinCustomersVillagerGeckoRenderer> geckoRenderer;

    public SkinCustomersVillagerClientAppearance(EntityRendererProvider.Context context) {
        wideRenderer = new SkinCustomersVillagerRenderer<>(context, false);
        slimRenderer = new SkinCustomersVillagerRenderer<>(context, true);
        geckoRenderer = Platform.isModLoaded("geckolib")
                ? Optional.of(SkinCustomersVillagerGeckoSupport.createRenderer(context))
                : Optional.empty();
    }

    @Override
    public EntityRenderer<?> getRenderer(CustomersVillager villager) {
        SkinCustomersVillagerModel model = getSkin(villager).model();
        if (model.isGecko()) {
            return geckoRenderer.<EntityRenderer<?>>map(renderer -> renderer.getRenderer(villager)).orElse(wideRenderer);
        }
        return model.equals(SkinCustomersVillagerModel.SLIM) ? slimRenderer : wideRenderer;
    }

    @Override
    public float getNameTagOffset(CustomersVillager villager) {
        return getSkin(villager).nameTagOffset();
    }

    @Override
    public float getShadowRadius(CustomersVillager villager) {
        return getSkin(villager).shadowRadius();
    }

    @Override
    public Vec3 getSittingOffset(CustomersVillager villager) {
        return geckoRenderer.map(renderer -> renderer.getSittingOffset(villager)).orElse(Vec3.ZERO);
    }

    @Override
    public Optional<Vec3> getOverheadAnchor(CustomersVillager villager) {
        return geckoRenderer.flatMap(renderer -> renderer.getOverheadAnchor(villager));
    }

    static SkinCustomersVillagerDefinition getSkin(CustomersVillager villager) {
        if (CustomersVillagerAppearances.get(villager) instanceof SkinPackCustomersVillagerAppearance appearance) {
            return appearance.getSkin(villager).orElseThrow();
        }
        throw new IllegalStateException("Skin renderer used for a non-skin appearance");
    }
}
