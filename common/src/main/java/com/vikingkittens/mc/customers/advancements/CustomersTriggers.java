package com.vikingkittens.mc.customers.advancements;

import net.minecraft.advancements.CriteriaTriggers;

import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCounterPlaced;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerSpawnerChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerLeaderboardChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerShiftFinished;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSupplierSpawnerChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSuppliesPurchased;

public final class CustomersTriggers {
    public static final CustomersTriggerItemServed ITEM_SERVED =
            CriteriaTriggers.register(new CustomersTriggerItemServed());
    public static final CustomersTriggerCustomerServed CUSTOMER_SERVED =
            CriteriaTriggers.register(new CustomersTriggerCustomerServed());
    public static final CustomersTriggerShiftFinished SHIFT_FINISHED =
            CriteriaTriggers.register(new CustomersTriggerShiftFinished());
    public static final CustomersTriggerLeaderboardChanged LEADERBOARD_CHANGED =
            CriteriaTriggers.register(new CustomersTriggerLeaderboardChanged());
    public static final CustomersTriggerSuppliesPurchased SUPPLIES_PURCHASED =
            CriteriaTriggers.register(new CustomersTriggerSuppliesPurchased());
    public static final CustomersTriggerCustomerSpawnerChanged CUSTOMER_SPAWNER_CHANGED =
            CriteriaTriggers.register(new CustomersTriggerCustomerSpawnerChanged());
    public static final CustomersTriggerSupplierSpawnerChanged SUPPLIER_SPAWNER_CHANGED =
            CriteriaTriggers.register(new CustomersTriggerSupplierSpawnerChanged());
    public static final CustomersTriggerCounterPlaced COUNTER_PLACED =
            CriteriaTriggers.register(new CustomersTriggerCounterPlaced());

    private CustomersTriggers() {}

    public static void initialize() {}
}
