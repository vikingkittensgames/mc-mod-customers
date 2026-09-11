package com.vikingkittens.mc.customers.economy;

import net.minecraft.resources.ResourceLocation;

record EconomyItemCostDefinition(
        EconomyItemMatcher matcher,
        ResourceLocation costItemId,
        int costCount
) {}
