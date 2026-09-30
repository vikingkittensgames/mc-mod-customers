package com.vikingkittens.mc.customers.compatability;

import java.util.Objects;

import dev.architectury.hooks.item.ItemStackHooks;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Provides version-compatible item stack lifecycle operations.
 */
public final class ItemStackCUtils {
    private ItemStackCUtils() {
    }
    public static void onCraftedBy(
            ItemStack stack,
            Player player,
            int count
    ) {
        stack.onCraftedBy(player, count);
    }
    public static ItemStack getCraftingRemainder(ItemStack stack) {
        return ItemStackHooks.hasCraftingRemainingItem(stack) ? ItemStackHooks.getCraftingRemainingItem(stack) : ItemStack.EMPTY;
    }

    public static boolean isSameItemAndTags(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameComponents(first, second);
    }

    public static boolean matchesCost(ItemCost cost, ItemStack stack) {
        return cost.test(stack);
    }
    /**
     * Creates an offer cost retaining the supplied stack's data components.
     *
     * @param stack stack defining the item and required components
     * @param count required item count
     * @return component-aware offer cost
     */
    public static ItemCost createItemCost(ItemStack stack, int count) {
        return new ItemCost(
                stack.getItem().builtInRegistryHolder(),
                count,
                DataComponentExactPredicate.allOf(
                        stack.getComponents().filter(componentType ->
                                !componentValuesEqual(
                                        stack.get(componentType),
                                        stack.getPrototype().get(componentType)
                                ))
                )
        );
    }

    private static boolean componentValuesEqual(
            Object value,
            Object prototypeValue
    ) {
        if (value instanceof ProvidesTrimMaterial valueMaterial
                && prototypeValue instanceof ProvidesTrimMaterial prototypeMaterial
                && (valueMaterial.material().key().isPresent()
                        || prototypeMaterial.material().key().isPresent())) {
            return valueMaterial.material().key().equals(
                    prototypeMaterial.material().key()
            );
        }
        if (value instanceof Holder<?> valueHolder
                && prototypeValue instanceof Holder<?> prototypeHolder) {
            if (valueHolder.unwrapKey().isPresent()
                    || prototypeHolder.unwrapKey().isPresent()) {
                return valueHolder.unwrapKey().equals(
                        prototypeHolder.unwrapKey()
                );
            }
            return Objects.equals(
                    valueHolder.value(),
                    prototypeHolder.value()
            );
        }
        return Objects.equals(value, prototypeValue);
    }

    /**
     * Rebuilds loaded offers with cost predicates containing only non-default
     * components while preserving their trade state.
     *
     * @param offers offers decoded from entity data
     * @return offers with normalized costs
     */
    public static MerchantOffers normalizeOfferCosts(MerchantOffers offers) {
        MerchantOffers normalizedOffers = new MerchantOffers();
        for (MerchantOffer offer : offers) {
            MerchantOffer normalizedOffer = new MerchantOffer(
                    createItemCost(
                            offer.getBaseCostA(),
                            offer.getItemCostA().count()
                    ),
                    offer.getItemCostB().map(cost -> createItemCost(
                            cost.itemStack(),
                            cost.count()
                    )),
                    offer.getResult().copy(),
                    offer.getUses(),
                    offer.getMaxUses(),
                    offer.getXp(),
                    offer.getPriceMultiplier(),
                    offer.getDemand()
            );
            normalizedOffer.setSpecialPriceDiff(
                    offer.getSpecialPriceDiff()
            );
            normalizedOffers.add(normalizedOffer);
        }
        return normalizedOffers;
    }
}
