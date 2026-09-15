package com.vikingkittens.mc.customers.advancements;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomersStatistics {
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> ITEM_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "item_served",
                    () -> ResourceLocation.fromNamespaceAndPath("customers", "item_served")
            );

    private CustomersStatistics() {}

    public static void initialize() {}

    public static final class ItemServed {
        private ItemServed() {}

        public static ResourceLocation id() {
            return ITEM_SERVED.getId();
        }
    }
}
