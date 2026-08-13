package com.vikingkittens.mc.customers.common;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OfferUtilsTest {
    @Test
    void reportsAllocationForBothOffersAndStacks() {
        OfferUtils.Allocation allocation =
                OfferUtils.allocateDetailed(
                        List.of(
                                offer(Items.COOKIE, 2),
                                offer(Items.COOKIE, 3),
                                offer(Items.BREAD, 4)
                        ),
                        List.of(
                                new ItemStack(Items.COOKIE, 4),
                                new ItemStack(Items.BREAD, 2)
                        )
                );

        assertEquals(
                List.of(2, 2, 2),
                allocation.offerCounts()
        );
        assertEquals(
                List.of(4, 2),
                allocation.stackCounts()
        );
    }

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void allocatesCombinedDemandAcrossStacksInInventoryOrder() {
        List<Integer> allocated = OfferUtils.allocate(
                List.of(
                        offer(Items.COOKIE, 5),
                        offer(Items.BREAD, 2)
                ),
                List.of(
                        new ItemStack(Items.COOKIE, 3),
                        new ItemStack(Items.COOKIE, 4),
                        new ItemStack(Items.BREAD, 4)
                )
        );

        assertEquals(List.of(3, 2, 2), allocated);
    }

    @Test
    void combinesMatchingDemandFromMultipleOffers() {
        List<Integer> allocated = OfferUtils.allocate(
                List.of(
                        offer(Items.COOKIE, 2),
                        offer(Items.COOKIE, 3)
                ),
                List.of(new ItemStack(Items.COOKIE, 8))
        );

        assertEquals(List.of(5), allocated);
    }

    @Test
    void subtractsEarlierStacksBeforeLaterStacks() {
        List<Integer> allocated = OfferUtils.allocate(
                List.of(offer(Items.COOKIE, 5)),
                List.of(
                        new ItemStack(Items.COOKIE, 3),
                        new ItemStack(Items.COOKIE, 4)
                )
        );

        assertEquals(List.of(3, 2), allocated);
    }

    @Test
    void ignoresCompletedOffers() {
        MerchantOffer completed = offer(Items.COOKIE, 5);
        completed.increaseUses();

        List<Integer> allocated = OfferUtils.allocate(
                List.of(completed),
                List.of(new ItemStack(Items.COOKIE, 5))
        );

        assertEquals(List.of(0), allocated);
    }

    @Test
    void allocatesOnlyStacksMatchingTheOfferItem() {
        List<Integer> allocated = OfferUtils.allocate(
                List.of(offer(Items.COOKIE, 3)),
                List.of(
                        new ItemStack(Items.BREAD, 3),
                        new ItemStack(Items.COOKIE, 3)
                )
        );

        assertEquals(List.of(0, 3), allocated);
    }

    private static MerchantOffer offer(Item item, int count) {
        return new MerchantOffer(
                new ItemStack(item, count),
                ItemStack.EMPTY,
                new ItemStack(Items.EMERALD),
                1,
                1,
                0.0F
        );
    }
}
