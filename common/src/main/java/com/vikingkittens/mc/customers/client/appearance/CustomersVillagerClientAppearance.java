package com.vikingkittens.mc.customers.client.appearance;

import java.util.Optional;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

public interface CustomersVillagerClientAppearance {
    EntityRenderer<?> getRenderer(CustomersVillager villager);

    default float getNameTagOffset(CustomersVillager villager) {
        return 0.0F;
    }

    default float getShadowRadius(CustomersVillager villager) {
        return 0.5F;
    }

    default Vec3 getSittingOffset(CustomersVillager villager) {
        return Vec3.ZERO;
    }

    default Optional<Vec3> getOverheadAnchor(CustomersVillager villager) {
        return Optional.empty();
    }
}
