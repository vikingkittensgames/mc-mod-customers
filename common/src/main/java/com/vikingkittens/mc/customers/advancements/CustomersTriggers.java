package com.vikingkittens.mc.customers.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCounterPlaced;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerSpawnerChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerLeaderboardChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerShiftFinished;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSupplierSpawnerChanged;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomersTriggers {
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerItemServed> ITEM_SERVED =
            CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "item_served",
                    CustomersTriggerItemServed::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerCustomerServed> CUSTOMER_SERVED =
            CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "customer_served",
                    CustomersTriggerCustomerServed::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerShiftFinished> SHIFT_FINISHED =
            CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "shift_finished",
                    CustomersTriggerShiftFinished::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerLeaderboardChanged>
            LEADERBOARD_CHANGED = CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "leaderboard_changed",
                    CustomersTriggerLeaderboardChanged::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerCustomerSpawnerChanged>
            CUSTOMER_SPAWNER_CHANGED = CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "customer_spawner_changed",
                    CustomersTriggerCustomerSpawnerChanged::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerSupplierSpawnerChanged>
            SUPPLIER_SPAWNER_CHANGED = CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "supplier_spawner_changed",
                    CustomersTriggerSupplierSpawnerChanged::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerCounterPlaced> COUNTER_PLACED =
            CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "counter_placed",
                    CustomersTriggerCounterPlaced::new
            );

    private CustomersTriggers() {}

    public static void initialize() {}
}
