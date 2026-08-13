package com.vikingkittens.mc.customers.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;

public final class OfferUtils {
    public record Allocation(
            List<Integer> offerCounts,
            List<Integer> stackCounts
    ) {
        public Allocation {
            offerCounts = List.copyOf(offerCounts);
            stackCounts = List.copyOf(stackCounts);
        }
    }

    private OfferUtils() {
    }

    public static List<Integer> allocate(
            List<MerchantOffer> offers,
            List<ItemStack> stacks
    ) {
        return allocateDetailed(offers, stacks).stackCounts();
    }

    public static Allocation allocateDetailed(
            List<MerchantOffer> offers,
            List<ItemStack> stacks
    ) {
        int[] remainingDemand = new int[offers.size()];
        int[] allocatedByOffer = new int[offers.size()];
        for (int offerIndex = 0;
                offerIndex < offers.size();
                offerIndex++) {
            MerchantOffer offer = offers.get(offerIndex);
            if (!offer.isOutOfStock()) {
                remainingDemand[offerIndex] =
                        offer.getCostA().getCount();
            }
        }

        List<Integer> allocatedCounts =
                new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            int stackRemaining = stack.getCount();
            int allocated = 0;
            for (int offerIndex = 0;
                    offerIndex < offers.size()
                            && stackRemaining > 0;
                    offerIndex++) {
                MerchantOffer offer = offers.get(offerIndex);
                if (remainingDemand[offerIndex] == 0
                        || !ItemStackCUtils.isSameItemAndTags(offer.getCostA(), stack)) {
                    continue;
                }
                int assigned = Math.min(
                        stackRemaining,
                        remainingDemand[offerIndex]
                );
                stackRemaining -= assigned;
                allocated += assigned;
                remainingDemand[offerIndex] -= assigned;
                allocatedByOffer[offerIndex] += assigned;
            }
            allocatedCounts.add(allocated);
        }

        List<Integer> offerCounts =
                new ArrayList<>(allocatedByOffer.length);
        for (int allocated : allocatedByOffer) {
            offerCounts.add(allocated);
        }
        return new Allocation(offerCounts, allocatedCounts);
    }
}
