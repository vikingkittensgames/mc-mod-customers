package com.vikingkittens.mc.customers.appearance;

import java.util.List;
import java.util.function.IntUnaryOperator;

public final class CustomersVillagerAppearanceSelector {
    private CustomersVillagerAppearanceSelector() {}

    public static <T extends CustomersVillagerAppearance> T selectApplicable(
            List<T> enabledAppearances,
            CustomersVillager villager,
            IntUnaryOperator randomIndex
    ) {
        List<T> applicableAppearances = enabledAppearances.stream()
                .filter(appearance -> appearance.isApplicable(villager))
                .toList();
        if (applicableAppearances.isEmpty()) {
            return null;
        }
        return applicableAppearances.get(
                randomIndex.applyAsInt(applicableAppearances.size())
        );
    }
}
