package com.vikingkittens.mc.customers.compatability;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class CustomersRegistryEntryTest {
    @Test
    void exposesIdentityAndResolvesItsValueLazily() {
        Identifier id =
                Identifier.fromNamespaceAndPath("customers", "test");
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item item = mock(Item.class);
        AtomicInteger resolutions = new AtomicInteger();
        CustomersRegistryEntry<Item, Item> entry = new CustomersRegistryEntry<>(
                key,
                () -> {
                    resolutions.incrementAndGet();
                    return item;
                }
        );

        assertEquals(key, entry.getKey());
        assertEquals(id, entry.getId());
        assertEquals(0, resolutions.get());
        assertSame(item, entry.get());
        assertEquals(1, resolutions.get());
    }
}
