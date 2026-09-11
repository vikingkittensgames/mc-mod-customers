package com.vikingkittens.mc.customers.economy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

public final class EconomyCurrency {
    private final List<EconomyItemCostDefinition> conversions;

    EconomyCurrency(List<EconomyItemCostDefinition> conversions) {
        this.conversions = List.copyOf(conversions);
    }

    public ItemStack convert(ItemStack source) {
        ConversionCandidate best = null;
        Set<EconomyItemMatcher> overriddenMatchers = new HashSet<>();
        for (int index = conversions.size() - 1; index >= 0; index--) {
            EconomyItemCostDefinition conversion = conversions.get(index);
            if (!overriddenMatchers.add(conversion.matcher()) || !conversion.matcher().matches(source)) {
                continue;
            }
            ConversionCandidate candidate = ConversionCandidate.create(source, conversion);
            if (best == null || candidate.isPreferredTo(best)) {
                best = candidate;
            }
        }
        return best == null
                ? source
                : EconomyJsonData.costStack(best.definition().costItemId(), best.roundedCount());
    }

    private record ConversionCandidate(
            EconomyItemCostDefinition definition,
            long targetNumerator,
            int targetDenominator,
            int distanceNumerator
    ) {
        private static ConversionCandidate create(ItemStack source, EconomyItemCostDefinition definition) {
            long numerator = (long) source.getCount() * definition.costCount();
            int denominator = definition.matcher().count();
            int remainder = (int)(numerator % denominator);
            int distance = Math.min(remainder, denominator - remainder);
            return new ConversionCandidate(definition, numerator, denominator, distance);
        }

        private boolean isPreferredTo(ConversionCandidate other) {
            long thisDistance = (long)distanceNumerator * other.targetDenominator;
            long otherDistance = (long)other.distanceNumerator * targetDenominator;
            if (thisDistance != otherDistance) {
                return thisDistance < otherDistance;
            }
            return definition.matcher().count() > other.definition.matcher().count();
        }

        private int roundedCount() {
            long rounded = (targetNumerator + targetDenominator / 2L) / targetDenominator;
            return Economy.safeCount(rounded);
        }
    }
}
