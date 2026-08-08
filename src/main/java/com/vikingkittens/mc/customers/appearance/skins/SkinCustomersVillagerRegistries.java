package com.vikingkittens.mc.customers.appearance.skins;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public final class SkinCustomersVillagerRegistries {
    public static final ResourceKey<Registry<SkinCustomersVillagerDefinition>> SKINS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Customers.MODID, "skins"));
    public static final ResourceKey<Registry<SkinPackCustomersVillagerDefinition>> SKIN_PACKS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Customers.MODID, "skin_packs"));

    private SkinCustomersVillagerRegistries() {}
}
