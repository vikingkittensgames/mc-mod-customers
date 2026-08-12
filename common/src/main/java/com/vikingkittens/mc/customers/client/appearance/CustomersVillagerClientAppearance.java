package com.vikingkittens.mc.customers.client.appearance;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

public interface CustomersVillagerClientAppearance {
    MobRenderer<?, ?> getRenderer(CustomersVillager villager);

    default float getNameTagOffset(CustomersVillager villager) {
        return 0.0F;
    }

    default Vec3 getSittingOffset(CustomersVillager villager) {
        return Vec3.ZERO;
    }
}
