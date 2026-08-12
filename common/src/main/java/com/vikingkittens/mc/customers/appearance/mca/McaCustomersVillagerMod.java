package com.vikingkittens.mc.customers.appearance.mca;

import java.util.function.Predicate;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class McaCustomersVillagerMod {
    public static final String MOD_ID = "mca";

    private McaCustomersVillagerMod() {}

    public static boolean isLoaded() {
        return CustomersServices.platform().isModLoaded(MOD_ID);
    }

    static boolean isLoaded(Predicate<String> loadedMods) {
        return loadedMods.test(MOD_ID);
    }
}
