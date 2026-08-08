package com.vikingkittens.mc.customers.appearance;

import java.util.Optional;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public interface CustomersVillager {
    RegistryAccess registryAccess();

    CustomersVillagerType getCustomersVillagerType();

    Optional<CustomerSpawnerMode> getSpawnerMode();

    boolean isSpecial();

    ResourceLocation getAppearanceId();

    void setAppearanceId(ResourceLocation appearanceId);

    float getVariationSeed();

    void setVariationSeed(float variationSeed);

    boolean isSitting();

    boolean isInWater();
}
