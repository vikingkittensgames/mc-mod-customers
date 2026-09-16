package com.vikingkittens.mc.customers.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerShiftFinished;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomersTriggers {
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerItemServed> ITEM_SERVED =
            CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "item_served",
                    CustomersTriggerItemServed::new
            );
    public static final CustomersRegistryEntry<CriterionTrigger<?>, CustomersTriggerShiftFinished> SHIFT_FINISHED =
            CustomersServices.registration().register(
                    Registries.TRIGGER_TYPE,
                    "shift_finished",
                    CustomersTriggerShiftFinished::new
            );

    private CustomersTriggers() {}

    public static void initialize() {}
}
