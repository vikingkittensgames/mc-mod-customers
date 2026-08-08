package com.vikingkittens.mc.customers.appearance.mca;

import java.util.function.Predicate;

import net.neoforged.fml.ModList;

public final class McaCustomersVillagerMod {
    public static final String MOD_ID = "mca";

    private McaCustomersVillagerMod() {}

    public static boolean isLoaded() {
        return isLoaded(ModList.get()::isLoaded);
    }

    static boolean isLoaded(Predicate<String> loadedMods) {
        return loadedMods.test(MOD_ID);
    }
}
