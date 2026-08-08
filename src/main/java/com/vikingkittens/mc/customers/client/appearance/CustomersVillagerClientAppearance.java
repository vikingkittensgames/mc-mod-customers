package com.vikingkittens.mc.customers.client.appearance;

import net.minecraft.client.renderer.entity.MobRenderer;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

public interface CustomersVillagerClientAppearance {
    MobRenderer<?, ?> getRenderer(CustomersVillager villager);

    default float getNameTagOffset(CustomersVillager villager) {
        return 0.0F;
    }
}
