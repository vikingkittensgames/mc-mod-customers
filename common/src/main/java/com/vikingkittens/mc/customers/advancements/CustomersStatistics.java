package com.vikingkittens.mc.customers.advancements;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomersStatistics {
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> ITEM_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "item_served",
                    () -> ResourceLocation.fromNamespaceAndPath(Customers.MODID, "item_served")
            );
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> SHIFT_FINISHED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "shift_finished",
                    () -> ResourceLocation.fromNamespaceAndPath(Customers.MODID, "shift_finished")
            );
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> CUSTOMER_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "customer_served",
                    () -> ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_served")
            );
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> CUSTOMER_CASUAL_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "customer_casual_served",
                    () -> ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_casual_served")
            );
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> CUSTOMER_NORMAL_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "customer_normal_served",
                    () -> ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_normal_served")
            );
    public static final CustomersRegistryEntry<ResourceLocation, ResourceLocation> CUSTOMER_IMPATIENT_SERVED =
            CustomersServices.registration().register(
                    Registries.CUSTOM_STAT,
                    "customer_impatient_served",
                    () -> ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_impatient_served")
            );

    private CustomersStatistics() {}

    public static void initialize() {}
}
