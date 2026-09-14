package com.vikingkittens.mc.customers.advancements;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomersStatistics {
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> CUSTOMER_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "customer_served",
                    () -> ResourceLocation.fromNamespaceAndPath("customers", "customer_served")
            );

    private CustomersStatistics() {}

    public static void initialize() {}

    public static final class CustomerServed {
        private CustomerServed() {}

        public static ResourceLocation id() {
            return CUSTOMER_SERVED.getId();
        }
    }
}
