package com.vikingkittens.mc.customers.customer;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomerInternalEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void protectsEventItemStacksFromMutation() {
        ItemStack servedItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        CustomerInternalEvents.CustomerServed event = new CustomerInternalEvents.CustomerServed(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer"),
                servedItem,
                costItem
        );

        servedItem.setCount(1);
        costItem.setCount(1);
        event.servedItem().setCount(64);
        event.costItem().setCount(64);

        assertEquals(3, event.servedItem().getCount());
        assertEquals(2, event.costItem().getCount());
    }
}
