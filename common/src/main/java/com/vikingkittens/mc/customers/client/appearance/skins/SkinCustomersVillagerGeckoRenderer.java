package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.Optional;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

interface SkinCustomersVillagerGeckoRenderer {
    EntityRenderer<?> getRenderer(CustomersVillager villager);

    Vec3 getSittingOffset(CustomersVillager villager);

    Optional<Vec3> getOverheadAnchor(CustomersVillager villager);
}
