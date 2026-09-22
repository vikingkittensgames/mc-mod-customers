package com.vikingkittens.mc.customers.appearance.mca;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceRegistries;

public final class McaCustomersVillagerAppearanceEvents {
    private static boolean initialized;

    private McaCustomersVillagerAppearanceEvents() {}

    public static void initialize() {
        if (initialized || !McaCustomersVillagerMod.isSupported()) {
            return;
        }
        CustomersVillagerAppearanceRegistries.appearances().register(
                McaCustomersVillagerAppearance.ID,
                McaCustomersVillagerAppearance::new);
        initialized = true;
    }
}
