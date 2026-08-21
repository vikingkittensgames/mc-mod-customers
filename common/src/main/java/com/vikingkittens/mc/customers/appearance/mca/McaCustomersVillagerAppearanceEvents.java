package com.vikingkittens.mc.customers.appearance.mca;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class McaCustomersVillagerAppearanceEvents {
    private static boolean initialized;

    private McaCustomersVillagerAppearanceEvents() {}

    public static void initialize() {
        if (initialized || !McaCustomersVillagerMod.isSupported()) {
            return;
        }
        CustomersServices.registration().register(
                CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
                McaCustomersVillagerAppearance.ID.getPath(),
                McaCustomersVillagerAppearance::new);
        initialized = true;
    }
}
