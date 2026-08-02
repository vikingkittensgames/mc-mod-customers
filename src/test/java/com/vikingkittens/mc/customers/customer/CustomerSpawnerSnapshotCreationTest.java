package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerSpawnerSnapshotCreationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void createsSnapshotWithEveryRemainingOfferCost() {
        MerchantOffer appleOffer = offer(Items.APPLE, 2);
        MerchantOffer breadOffer = offer(Items.BREAD, 4);
        MerchantOffer completedOffer = offer(Items.CARROT, 6);
        completedOffer.increaseUses();
        MerchantOffers offers = new MerchantOffers();
        offers.add(appleOffer);
        offers.add(completedOffer);
        offers.add(breadOffer);
        UUID customerId = UUID.randomUUID();
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        when(customer.getUUID()).thenReturn(customerId);
        when(customer.getSnapshotType()).thenReturn(
                CustomerSpawnerSnapshot.Customer.Type.IMPATIENT
        );
        when(customer.getOffers()).thenReturn(offers);
        BlockPos spawnerPos = new BlockPos(10, 64, -20);
        UUID bossEventId = UUID.randomUUID();

        CustomerSpawnerSnapshot snapshot =
                CustomerSpawnerSnapshot.create(
                        spawnerPos,
                        CustomerSpawnerMode.LUNCH,
                        true,
                        Optional.of(bossEventId),
                        List.of(customer)
                );

        assertEquals(spawnerPos, snapshot.spawnerPos());
        assertEquals(CustomerSpawnerMode.LUNCH, snapshot.spawnerMode());
        assertTrue(snapshot.specialEnabled());
        assertEquals(Optional.of(bossEventId), snapshot.bossEventId());
        assertEquals(1, snapshot.customers().size());
        CustomerSpawnerSnapshot.Customer customerSnapshot =
                snapshot.customers().getFirst();
        assertEquals(customerId, customerSnapshot.customerId());
        assertEquals(
                CustomerSpawnerSnapshot.Customer.Type.IMPATIENT,
                customerSnapshot.type()
        );
        assertEquals(2, customerSnapshot.offerCostItems().size());
        assertEquals(2, customerSnapshot.offerCostItems().get(0).getCount());
        assertEquals(4, customerSnapshot.offerCostItems().get(1).getCount());
        assertTrue(ItemStack.isSameItemSameComponents(
                appleOffer.getCostA(),
                customerSnapshot.offerCostItems().get(0)
        ));
        assertTrue(ItemStack.isSameItemSameComponents(
                breadOffer.getCostA(),
                customerSnapshot.offerCostItems().get(1)
        ));
        assertNotSame(
                appleOffer.getCostA(),
                customerSnapshot.offerCostItems().get(0)
        );
        assertFalse(customerSnapshot.offerCostItems().contains(
                completedOffer.getCostA()
        ));
    }

    private static MerchantOffer offer(
            net.minecraft.world.item.Item item,
            int count
    ) {
        return new MerchantOffer(
                new ItemCost(item, count),
                Optional.empty(),
                new ItemStack(Items.EMERALD),
                1,
                1,
                0
        );
    }
}
