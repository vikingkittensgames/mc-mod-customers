package com.vikingkittens.mc.customers.advancements.triggers;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersTriggerCustomerServedTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesAnEventWhenAllOptionalConditionsAreAbsent() {
        CustomersTriggerCustomerServed.Instance instance =
                new CustomersTriggerCustomerServed.Instance(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                );

        assertTrue(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, false),
                1,
                0,
                1,
                0
        ));
    }

    @Test
    void matchesEventAndCustomerTotalsTogether() {
        CustomersTriggerCustomerServed.Instance instance =
                new CustomersTriggerCustomerServed.Instance(
                        Optional.empty(),
                        Optional.of(CustomerSpawnerMode.LUNCH),
                        Optional.of(ResourceLocation.parse("customers:customer_impatient")),
                        Optional.empty(),
                        Optional.of(MinMaxBounds.Ints.atLeast(3)),
                        Optional.empty(),
                        Optional.of(MinMaxBounds.Ints.exactly(2)),
                        Optional.of(true),
                        Optional.of(MinMaxBounds.Ints.atLeast(100)),
                        Optional.of(MinMaxBounds.Ints.exactly(25)),
                        Optional.of(MinMaxBounds.Ints.exactly(50)),
                        Optional.of(MinMaxBounds.Ints.atLeast(25))
                );

        assertTrue(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.DINNER, 3, 2, true),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 2, 2, true),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 1, true),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, false),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                99,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                100,
                24,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                100,
                25,
                49,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                100,
                25,
                50,
                24
        ));
    }

    @Test
    void eventMatchingDoesNotDependOnPlayerLifetimeTotals() {
        CustomersTriggerCustomerServed.Instance instance =
                new CustomersTriggerCustomerServed.Instance(
                        Optional.empty(),
                        Optional.of(CustomerSpawnerMode.LUNCH),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(MinMaxBounds.Ints.atLeast(100)),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                );

        assertTrue(instance.matchesEvent(event(CustomerSpawnerMode.LUNCH, 3, 2, false)));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, false),
                99,
                0,
                0,
                0
        ));
    }

    @Test
    void schemaDescribesAndUpdatesTriggerProperties() {
        assertEquals("spawner_location", CustomersTriggerCustomerServed.SCHEMA.properties().get(1).serializedName());
        assertEquals("spawner_mode", CustomersTriggerCustomerServed.SCHEMA.properties().get(2).serializedName());
        assertEquals(
                CustomersTriggerSchema.Editor.OPTIONAL_LOCATION,
                CustomersTriggerCustomerServed.SCHEMA.properties().get(1).editor()
        );
        CustomersTriggerSchema.Property<CustomersTriggerCustomerServed.Instance> spawnerMode =
                CustomersTriggerCustomerServed.SCHEMA.property("spawner_mode").orElseThrow();

        assertEquals(CustomersTriggerSchema.Editor.OPTIONAL_ENUM, spawnerMode.editor());
        assertTrue(spawnerMode.taskEditable());
        assertFalse(CustomersTriggerCustomerServed.SCHEMA
                .property("total_customers_served")
                .orElseThrow()
                .taskEditable());
        assertFalse(CustomersTriggerCustomerServed.SCHEMA
                .property("total_casual_customers_served")
                .orElseThrow()
                .taskEditable());
        assertFalse(CustomersTriggerCustomerServed.SCHEMA
                .property("total_normal_customers_served")
                .orElseThrow()
                .taskEditable());
        assertFalse(CustomersTriggerCustomerServed.SCHEMA
                .property("total_impatient_customers_served")
                .orElseThrow()
                .taskEditable());

        CustomersTriggerCustomerServed.Instance updated = CustomersTriggerCustomerServed.SCHEMA.with(
                CustomersTriggerCustomerServed.Instance.ANY,
                spawnerMode,
                Optional.of(CustomerSpawnerMode.LUNCH)
        );

        assertEquals(Optional.of(CustomerSpawnerMode.LUNCH), updated.spawnerMode());
    }

    @Test
    void decodesDataDrivenCustomerServedConditions() {
        String json = """
                {
                  "spawner_mode": "lunch",
                  "customer_profession": "customers:customer_impatient",
                  "served_item": { "items": ["minecraft:apple"] },
                  "served_count": { "min": 3 },
                  "cost_item": { "items": ["minecraft:emerald"] },
                  "cost_count": 2,
                  "is_pet_item": true,
                  "total_customers_served": { "min": 100 },
                  "total_casual_customers_served": 25,
                  "total_normal_customers_served": 50,
                  "total_impatient_customers_served": { "min": 25 }
                }
                """;

        CustomersTriggerCustomerServed.Instance instance =
                CustomersTriggerCustomerServed.Instance.CODEC
                        .parse(
                                RegistryOps.create(
                                        JsonOps.INSTANCE,
                                        RegistryAccess.fromRegistryOfRegistries(
                                                BuiltInRegistries.REGISTRY
                                        )
                                ),
                                JsonParser.parseString(json)
                        )
                        .getOrThrow();

        assertTrue(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, false),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 3, true),
                100,
                25,
                50,
                25
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 3, 2, true),
                100,
                25,
                50,
                24
        ));
    }

    private static CustomerInternalEvents.CustomerServed event(
            CustomerSpawnerMode mode,
            int servedCount,
            int costCount,
            boolean isPetItem
    ) {
        return new CustomerInternalEvents.CustomerServed(
                mock(ServerLevel.class),
                null,
                mode,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer_impatient"),
                new ItemStack(Items.APPLE, servedCount),
                new ItemStack(Items.EMERALD, costCount),
                isPetItem
        );
    }
}
