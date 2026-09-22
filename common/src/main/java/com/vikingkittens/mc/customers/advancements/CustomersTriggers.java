package com.vikingkittens.mc.customers.advancements;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCounterPlaced;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerSpawnerChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerLeaderboardChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerShiftFinished;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSupplierSpawnerChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSuppliesPurchased;

public final class CustomersTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Customers.MODID, Registries.TRIGGER_TYPE);

    public static final RegistrySupplier<CustomersTriggerItemServed> ITEM_SERVED =
            TRIGGERS.register("item_served", CustomersTriggerItemServed::new);
    public static final RegistrySupplier<CustomersTriggerCustomerServed> CUSTOMER_SERVED =
            TRIGGERS.register("customer_served", CustomersTriggerCustomerServed::new);
    public static final RegistrySupplier<CustomersTriggerShiftFinished> SHIFT_FINISHED =
            TRIGGERS.register("shift_finished", CustomersTriggerShiftFinished::new);
    public static final RegistrySupplier<CustomersTriggerLeaderboardChanged> LEADERBOARD_CHANGED =
            TRIGGERS.register("leaderboard_changed", CustomersTriggerLeaderboardChanged::new);
    public static final RegistrySupplier<CustomersTriggerSuppliesPurchased> SUPPLIES_PURCHASED =
            TRIGGERS.register("supplies_purchased", CustomersTriggerSuppliesPurchased::new);
    public static final RegistrySupplier<CustomersTriggerCustomerSpawnerChanged> CUSTOMER_SPAWNER_CHANGED =
            TRIGGERS.register("customer_spawner_changed", CustomersTriggerCustomerSpawnerChanged::new);
    public static final RegistrySupplier<CustomersTriggerSupplierSpawnerChanged> SUPPLIER_SPAWNER_CHANGED =
            TRIGGERS.register("supplier_spawner_changed", CustomersTriggerSupplierSpawnerChanged::new);
    public static final RegistrySupplier<CustomersTriggerCounterPlaced> COUNTER_PLACED =
            TRIGGERS.register("counter_placed", CustomersTriggerCounterPlaced::new);

    private CustomersTriggers() {}

    public static void initialize() {
        TRIGGERS.register();
    }
}
