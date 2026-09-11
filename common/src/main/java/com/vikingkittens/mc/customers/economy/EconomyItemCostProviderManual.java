package com.vikingkittens.mc.customers.economy;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public final class EconomyItemCostProviderManual implements EconomyItemCostProvider {
    private final List<EconomyItemCostDefinition> definitions;

    EconomyItemCostProviderManual(List<EconomyItemCostDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    @Nullable
    @Override
    public ItemStack calculateItemStackCost(ItemStack item) {
        for (int index = definitions.size() - 1; index >= 0; index--) {
            EconomyItemCostDefinition definition = definitions.get(index);
            if (definition.matcher().matches(item)) {
                long cost = ((long) item.getCount() * definition.costCount()
                        + definition.matcher().count() - 1) / definition.matcher().count();
                return EconomyJsonData.costStack(definition.costItemId(), Economy.safeCount(cost));
            }
        }
        return null;
    }
}
