package com.vikingkittens.mc.customers.appearance.skins;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class SkinCustomersVillagerAppearanceEvents {
    private static boolean initialized;

    private SkinCustomersVillagerAppearanceEvents() {}

    public static void initialize() {
        if (initialized) {
            return;
        }
        CustomersVillagerAppearances.registerProvider(SkinPackCustomersVillagerAppearanceProvider.INSTANCE);
        CustomersServices.registration().registerDataPackRegistry(
                SkinCustomersVillagerRegistries.SKINS,
                SkinCustomersVillagerDefinition.CODEC);
        CustomersServices.registration().registerDataPackRegistry(
                SkinCustomersVillagerRegistries.SKIN_PACKS,
                SkinPackCustomersVillagerDefinition.CODEC);
        initialized = true;
    }
}
