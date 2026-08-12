package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerModel;
import com.vikingkittens.mc.customers.appearance.skins.SkinPackCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearance;

public final class SkinCustomersVillagerClientAppearance implements CustomersVillagerClientAppearance {
    private final MobRenderer<?, ?> wideRenderer;
    private final MobRenderer<?, ?> slimRenderer;

    public SkinCustomersVillagerClientAppearance(EntityRendererProvider.Context context) {
        wideRenderer = new SkinCustomersVillagerRenderer<>(context, false);
        slimRenderer = new SkinCustomersVillagerRenderer<>(context, true);
    }

    @Override
    public MobRenderer<?, ?> getRenderer(CustomersVillager villager) {
        return getSkin(villager).model() == SkinCustomersVillagerModel.SLIM ? slimRenderer : wideRenderer;
    }

    @Override
    public float getNameTagOffset(CustomersVillager villager) {
        return getSkin(villager).nameTagOffset();
    }

    static SkinCustomersVillagerDefinition getSkin(CustomersVillager villager) {
        if (CustomersVillagerAppearances.get(villager) instanceof SkinPackCustomersVillagerAppearance appearance) {
            return appearance.getSkin(villager).orElseThrow();
        }
        throw new IllegalStateException("Skin renderer used for a non-skin appearance");
    }
}
