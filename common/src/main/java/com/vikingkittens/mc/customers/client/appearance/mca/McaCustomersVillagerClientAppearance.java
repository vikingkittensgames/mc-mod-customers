package com.vikingkittens.mc.customers.client.appearance.mca;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearance;

public final class McaCustomersVillagerClientAppearance
        implements CustomersVillagerClientAppearance {
    private final McaCustomersVillagerRenderer<?> renderer;

    public McaCustomersVillagerClientAppearance(
            EntityRendererProvider.Context context
    ) {
        renderer = new McaCustomersVillagerRenderer<>(context);
    }

    @Override
    public EntityRenderer<?> getRenderer(
            CustomersVillager villager
    ) {
        return renderer;
    }

    @Override
    public Vec3 getSittingOffset(CustomersVillager villager) {
        return renderer.getSittingOffset(villager);
    }
}
