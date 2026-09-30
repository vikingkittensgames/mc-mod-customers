package com.vikingkittens.mc.customers.advancements;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.Customers;

public final class CustomersStatistics {
    private static final DeferredRegister<Identifier> STATISTICS =
            DeferredRegister.create(Customers.MODID, Registries.CUSTOM_STAT);

    public static final RegistrySupplier<Identifier> ITEM_SERVED =
            STATISTICS.register("item_served", () -> id("item_served"));
    public static final RegistrySupplier<Identifier> PET_ITEM_SERVED =
            STATISTICS.register("pet_item_served", () -> id("pet_item_served"));
    public static final RegistrySupplier<Identifier> SHIFT_FINISHED =
            STATISTICS.register("shift_finished", () -> id("shift_finished"));
    public static final RegistrySupplier<Identifier> CUSTOMER_SERVED =
            STATISTICS.register("customer_served", () -> id("customer_served"));
    public static final RegistrySupplier<Identifier> CUSTOMER_CASUAL_SERVED =
            STATISTICS.register("customer_casual_served", () -> id("customer_casual_served"));
    public static final RegistrySupplier<Identifier> CUSTOMER_NORMAL_SERVED =
            STATISTICS.register("customer_normal_served", () -> id("customer_normal_served"));
    public static final RegistrySupplier<Identifier> CUSTOMER_IMPATIENT_SERVED =
            STATISTICS.register("customer_impatient_served", () -> id("customer_impatient_served"));
    public static final RegistrySupplier<Identifier> SUPPLIES_PURCHASED =
            STATISTICS.register("supplies_purchased", () -> id("supplies_purchased"));

    private CustomersStatistics() {}

    public static void initialize() {
        STATISTICS.register();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Customers.MODID, path);
    }
}
