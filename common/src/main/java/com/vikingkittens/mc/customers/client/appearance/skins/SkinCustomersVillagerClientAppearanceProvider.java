package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.skins.SkinPackCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearance;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearanceProvider;

public final class SkinCustomersVillagerClientAppearanceProvider
        implements CustomersVillagerClientAppearanceProvider {
    public static final SkinCustomersVillagerClientAppearanceProvider INSTANCE =
            new SkinCustomersVillagerClientAppearanceProvider();

    private SkinCustomersVillagerClientAppearanceProvider() {
    }

    @Override
    public boolean supports(
            CustomersVillagerAppearance appearance
    ) {
        return appearance
                instanceof SkinPackCustomersVillagerAppearance;
    }

    @Override
    public CustomersVillagerClientAppearance create(
            EntityRendererProvider.Context context
    ) {
        return new SkinCustomersVillagerClientAppearance(context);
    }
}
