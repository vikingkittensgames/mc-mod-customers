package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Verifies crafted item assignment against customer demand. */
class CustomerCraftedOfferAssignmentTest {
    /** Initializes Minecraft item registries used by offer tests. */
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    /** Leaves a stack unchanged when no offer wants its item. */
    @Test
    void returnsOriginalStackWhenNoOfferMatches() {
        ItemStack apples = new ItemStack(Items.APPLE, 4);

        ItemStack remainder =
                CustomerVillagerEntity.tryAssignCraftedOffer(
                        List.of(offer(Items.BREAD, 4)),
                        new ArrayList<>(),
                        apples
                );

        assertSame(apples, remainder);
    }

    /** Assigns an entire stack when matching demand is sufficient. */
    @Test
    void assignsTheEntireStack() {
        List<ItemStack> crafted = new ArrayList<>();

        ItemStack remainder =
                CustomerVillagerEntity.tryAssignCraftedOffer(
                        List.of(offer(Items.BREAD, 25)),
                        crafted,
                        new ItemStack(Items.BREAD, 25)
                );

        assertNull(remainder);
        assertEquals(25, crafted.getFirst().getCount());
    }

    /** Returns only the quantity beyond matching customer demand. */
    @Test
    void returnsTheUnassignedRemainder() {
        List<ItemStack> crafted = new ArrayList<>();

        ItemStack remainder =
                CustomerVillagerEntity.tryAssignCraftedOffer(
                        List.of(offer(Items.BREAD, 15)),
                        crafted,
                        new ItemStack(Items.BREAD, 20)
                );

        assertEquals(5, remainder.getCount());
        assertEquals(15, crafted.getFirst().getCount());
    }

    /** Prevents repeated deposits from exceeding the offered quantity. */
    @Test
    void accountsForItemsAlreadyAssigned() {
        List<ItemStack> crafted = new ArrayList<>();
        List<MerchantOffer> offers =
                List.of(offer(Items.BREAD, 10));

        assertNull(CustomerVillagerEntity.tryAssignCraftedOffer(
                offers,
                crafted,
                new ItemStack(Items.BREAD, 6)
        ));
        assertNull(CustomerVillagerEntity.tryAssignCraftedOffer(
                offers,
                crafted,
                new ItemStack(Items.BREAD, 4)
        ));
        ItemStack extra = new ItemStack(Items.BREAD);

        assertSame(
                extra,
                CustomerVillagerEntity.tryAssignCraftedOffer(
                        offers,
                        crafted,
                        extra
                )
        );
        assertEquals(10, crafted.getFirst().getCount());
    }

    /** Combines demand from multiple active offers for the same item. */
    @Test
    void assignsAcrossMultipleMatchingOffers() {
        List<ItemStack> crafted = new ArrayList<>();

        ItemStack remainder =
                CustomerVillagerEntity.tryAssignCraftedOffer(
                        List.of(
                                offer(Items.BREAD, 8),
                                offer(Items.BREAD, 7),
                                offer(Items.APPLE, 20)
                        ),
                        crafted,
                        new ItemStack(Items.BREAD, 18)
                );

        assertEquals(3, remainder.getCount());
        assertEquals(15, crafted.getFirst().getCount());
    }

    /** Previews only outstanding demand without mutating assigned quantities. */
    @Test
    void previewsAssignableCountWithoutChangingCraftedItems() {
        List<ItemStack> crafted = new ArrayList<>();
        crafted.add(new ItemStack(Items.BREAD, 4));

        int assignable =
                CustomerVillagerEntity.getAssignableCraftedItemCount(
                        List.of(offer(Items.BREAD, 10)),
                        crafted,
                        new ItemStack(Items.BREAD, 10)
                );

        assertEquals(6, assignable);
        assertEquals(4, crafted.getFirst().getCount());
    }

    /** Creates a single-use customer offer for assignment tests. */
    private static MerchantOffer offer(Item item, int count) {
        return new MerchantOffer(
                new ItemCost(item, count),
                Optional.empty(),
                new ItemStack(Items.EMERALD),
                1,
                1,
                0.0F
        );
    }
}
