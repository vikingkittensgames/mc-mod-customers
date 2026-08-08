package com.vikingkittens.mc.customers.client.appearance;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;

/**
 * Supplies a client renderer for dynamically resolved appearance definitions.
 */
public interface CustomersVillagerClientAppearanceProvider {
    boolean supports(CustomersVillagerAppearance appearance);

    CustomersVillagerClientAppearance create(
            EntityRendererProvider.Context context
    );
}
