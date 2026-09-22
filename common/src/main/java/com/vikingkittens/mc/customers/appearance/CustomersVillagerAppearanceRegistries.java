package com.vikingkittens.mc.customers.appearance;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public final class CustomersVillagerAppearanceRegistries {
    private static final Registrar<CustomersVillagerAppearance> APPEARANCES = RegistrarManager.get(Customers.MODID)
            .<CustomersVillagerAppearance>builder(
                    ResourceLocation.fromNamespaceAndPath(Customers.MODID, "villager_appearance"))
            .syncToClients()
            .build();

    private CustomersVillagerAppearanceRegistries() {}

    public static Registrar<CustomersVillagerAppearance> appearances() {
        return APPEARANCES;
    }
}
