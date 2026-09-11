package com.vikingkittens.mc.customers.economy;

import java.util.List;

record EconomyData(
        List<EconomyItemCostDefinition> itemCosts,
        List<EconomyItemCostDefinition> currencyConversions
) {
    EconomyData {
        itemCosts = List.copyOf(itemCosts);
        currencyConversions = List.copyOf(currencyConversions);
    }
}
