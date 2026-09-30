package com.vikingkittens.mc.customers.economy;

import net.minecraft.resources.Identifier;

record EconomyItemCostDefinition(
        EconomyItemMatcher matcher,
        Identifier costItemId,
        int costCount
) {}
