package com.vikingkittens.mc.customers.advancements;

import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersTriggersTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesAnEventWhenAllOptionalConditionsAreAbsent() {
        CustomersTriggers.ItemServed.Instance instance = new CustomersTriggers.ItemServed.Instance(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2), 1));
    }

    @Test
    void matchesModeProfessionAndStackCountsTogether() {
        CustomersTriggers.ItemServed.Instance instance = new CustomersTriggers.ItemServed.Instance(
                Optional.empty(),
                Optional.of(CustomerSpawnerMode.LUNCH),
                Optional.of(ResourceLocation.parse("customers:customer_impatient")),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.atLeast(3)),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.exactly(2)),
                Optional.of(MinMaxBounds.Ints.atLeast(100))
        );

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2), 100));
        assertFalse(instance.matches(event(CustomerSpawnerMode.DINNER, 3, 2), 100));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 2, 2), 100));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 1), 100));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2), 99));
    }

    @Test
    void decodesDataDrivenItemServedConditions() {
        String json = """
                {
                  "spawner_mode": "lunch",
                  "customer_profession": "customers:customer_impatient",
                  "served_item": { "items": ["minecraft:apple"] },
                  "served_count": { "min": 3 },
                  "cost_item": { "items": ["minecraft:emerald"] },
                  "cost_count": 2,
                  "total_items_served": { "min": 100 }
                }
                """;

        CustomersTriggers.ItemServed.Instance instance = CustomersTriggers.ItemServed.Instance.CODEC
                .parse(
                        RegistryOps.create(
                                JsonOps.INSTANCE,
                                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                        ),
                        JsonParser.parseString(json)
                )
                .getOrThrow();

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2), 100));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 3), 100));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2), 99));
    }

    private static CustomerInternalEvents.ItemServed event(
            CustomerSpawnerMode mode,
            int servedCount,
            int costCount
    ) {
        return new CustomerInternalEvents.ItemServed(
                mock(ServerLevel.class),
                null,
                mode,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer_impatient"),
                new ItemStack(Items.APPLE, servedCount),
                new ItemStack(Items.EMERALD, costCount)
        );
    }
}
