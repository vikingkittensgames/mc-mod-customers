package com.vikingkittens.mc.customers.client.appearance.mca;

import java.util.List;
import java.util.function.ToDoubleFunction;

final class McaCustomersVillagerWeightedSelector {
    private McaCustomersVillagerWeightedSelector() {}

    static <T> T select(
            List<T> entries,
            ToDoubleFunction<T> weight,
            float choice,
            T fallback
    ) {
        double totalWeight = entries.stream()
                .mapToDouble(weight)
                .sum();
        if (totalWeight <= 0.0D) {
            return fallback;
        }

        double remaining = choice * totalWeight;
        for (T entry : entries) {
            remaining -= weight.applyAsDouble(entry);
            if (remaining < 0.0D) {
                return entry;
            }
        }
        return fallback;
    }
}
