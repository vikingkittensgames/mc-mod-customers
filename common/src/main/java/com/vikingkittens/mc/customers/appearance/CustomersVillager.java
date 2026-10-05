package com.vikingkittens.mc.customers.appearance;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public interface CustomersVillager {
    RegistryAccess getCustomersRegistryAccess();

    Level getCustomersLevel();

    @Nullable BlockPos getCustomersSpawnerPosition();

    CustomersVillagerType getCustomersVillagerType();

    Optional<CustomerSpawnerMode> getSpawnerMode();

    boolean isSpecial();

    ResourceLocation getAppearanceId();

    void setAppearanceId(ResourceLocation appearanceId);

    float getVariationSeed();

    void setVariationSeed(float variationSeed);

    Map<String, String> getAdditionalProperties();

    void setAdditionalProperties(Map<String, String> additionalProperties);

    void setVillagerName(Component name);

    boolean isVillagerSitting();

    boolean isVillagerInWater();
}
