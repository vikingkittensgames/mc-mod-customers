package com.vikingkittens.mc.customers.customer;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomerInternalEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void testItemServedCreation() {
        ItemStack servedItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        CustomerInternalEvents.ItemServed event = new CustomerInternalEvents.ItemServed(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer"),
                servedItem,
                costItem,
                false
        );

        servedItem.setCount(1);
        costItem.setCount(1);
        event.servedItem().setCount(64);
        event.costItem().setCount(64);

        assertEquals(3, event.servedItem().getCount());
        assertEquals(2, event.costItem().getCount());
        assertFalse(event.isPetItem());
    }

    @Test
    void testCustomerServedCreation() {
        ServerLevel level = mock(ServerLevel.class);
        UUID playerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ResourceLocation profession = ResourceLocation.parse("customers:customer");
        ItemStack servedItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        CustomerInternalEvents.CustomerServed event = new CustomerInternalEvents.CustomerServed(
                level,
                null,
                CustomerSpawnerMode.LUNCH,
                playerId,
                customerId,
                profession,
                servedItem,
                costItem,
                false
        );

        assertInstanceOf(CustomerInternalEvents.ServedEvent.class, event);
        assertEquals(level, event.level());
        assertEquals(CustomerSpawnerMode.LUNCH, event.spawnerMode());
        assertEquals(playerId, event.playerId());
        assertEquals(customerId, event.customerId());
        assertEquals(profession, event.customerProfession());
        assertTrue(ItemStack.isSameItemSameComponents(servedItem, event.servedItem()));
        assertEquals(servedItem.getCount(), event.servedItem().getCount());
        assertTrue(ItemStack.isSameItemSameComponents(costItem, event.costItem()));
        assertEquals(costItem.getCount(), event.costItem().getCount());
        assertFalse(event.isPetItem());
    }

    @Test
    void testShiftFinishedCreation() {
        CustomerInternalEvents.ShiftFinished event = new CustomerInternalEvents.ShiftFinished(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                1,
                0.75F,
                3,
                2,
                1,
                10,
                Map.of(
                        UUID.randomUUID(), 3,
                        UUID.randomUUID(), 2
                ),
                Map.of(
                        UUID.randomUUID(), 5,
                        UUID.randomUUID(), 3
                ),
                0,
                0
        );
    }
}
