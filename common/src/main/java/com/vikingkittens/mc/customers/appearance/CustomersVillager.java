package com.vikingkittens.mc.customers.appearance;

import java.util.Optional;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public interface CustomersVillager {
    RegistryAccess registryAccess();

    CustomersVillagerType getCustomersVillagerType();

    Optional<CustomerSpawnerMode> getSpawnerMode();

    boolean isSpecial();

    Identifier getAppearanceId();

    void setAppearanceId(Identifier appearanceId);

    float getVariationSeed();

    void setVariationSeed(float variationSeed);

    boolean isVillagerSitting();

    boolean isVillagerInWater();
}
