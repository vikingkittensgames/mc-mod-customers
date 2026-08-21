package com.vikingkittens.mc.customers.appearance.mca;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class McaCustomersVillagerMod {
    public static final String MOD_ID = "mca";

    private McaCustomersVillagerMod() {}

    public static boolean isLoaded() {
        return CustomersServices.platform().isModLoaded(MOD_ID);
    }

    public static boolean isSupported() {
        return isSupported(
                CustomersServices.platform()::isModLoaded,
                McaCustomersVillagerMod::hasHairPoolApi
        );
    }

    static boolean isLoaded(Predicate<String> loadedMods) {
        return loadedMods.test(MOD_ID);
    }

    static boolean isSupported(
            Predicate<String> loadedMods,
            BooleanSupplier hasHairList
    ) {
        return isLoaded(loadedMods) && hasHairList.getAsBoolean();
    }

    private static boolean hasHairPoolApi() {
        return hasClass("net.conczin.mca.resources.HairStyleList") ||
                hasClass("net.conczin.mca.resources.HairList");
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(
                    className,
                    false,
                    McaCustomersVillagerMod.class.getClassLoader()
            );
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }
}
