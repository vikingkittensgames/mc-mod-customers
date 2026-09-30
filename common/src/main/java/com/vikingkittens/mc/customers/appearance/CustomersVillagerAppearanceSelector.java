package com.vikingkittens.mc.customers.appearance;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import net.minecraft.resources.Identifier;

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

    public static Identifier selectApplicableId(
            List<Identifier> appearanceIds,
            Function<Identifier, CustomersVillagerAppearance> resolver,
            CustomersVillager villager,
            IntUnaryOperator randomIndex
    ) {
        List<Identifier> applicable = appearanceIds.stream()
                .filter(appearanceId -> {
                    CustomersVillagerAppearance appearance =
                            resolver.apply(appearanceId);
                    return appearance != null
                            && appearance.isApplicable(villager);
                })
                .toList();
        return applicable.isEmpty()
                ? null
                : applicable.get(randomIndex.applyAsInt(applicable.size()));
    }
}
