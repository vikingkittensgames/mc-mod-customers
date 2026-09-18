package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersTriggerItemServedTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesAnEventWhenAllOptionalConditionsAreAbsent() {
        CustomersTriggerItemServed.Instance instance = new CustomersTriggerItemServed.Instance(
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

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, false), 1, 0));
    }

    @Test
    void matchesModeProfessionAndStackCountsTogether() {
        CustomersTriggerItemServed.Instance instance = new CustomersTriggerItemServed.Instance(
                Optional.empty(),
                Optional.of(CustomerSpawnerMode.LUNCH),
                Optional.of(ResourceLocation.parse("customers:customer_impatient")),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.atLeast(3)),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.exactly(2)),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.atLeast(100)),
                Optional.empty()
        );

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, false), 100, 0));
        assertFalse(instance.matches(event(CustomerSpawnerMode.DINNER, 3, 2, false), 100, 0));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 2, 2, false), 100, 0));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 1, false), 100, 0));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, false), 99, 0));
    }

    @Test
    void matchesPetItemCondition() {
        CustomersTriggerItemServed.Instance instance = new CustomersTriggerItemServed.Instance(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(true),
                Optional.empty(),
                Optional.empty()
        );

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, true), 1, 1));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, false), 1, 1));
    }

    @Test
    void matchesSpawnerLocationCondition() {
        BlockPos position = new BlockPos(-120, 64, 350);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        CustomersTriggerSchema.Property<CustomersTriggerItemServed.Instance> property =
                CustomersTriggerItemServed.SCHEMA.property("spawner_location").orElseThrow();
        CustomersTriggerItemServed.Instance instance = CustomersTriggerItemServed.SCHEMA.with(
                CustomersTriggerItemServed.Instance.ANY,
                property,
                Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, position))
        );

        assertTrue(instance.matchesEvent(event(level, position, CustomerSpawnerMode.LUNCH, 3, 2, false)));
        assertFalse(instance.matchesEvent(event(level, position.above(), CustomerSpawnerMode.LUNCH, 3, 2, false)));
        assertFalse(instance.matchesEvent(event(level, null, CustomerSpawnerMode.LUNCH, 3, 2, false)));
    }

    @Test
    void eventMatchingDoesNotDependOnThePlayerLifetimeTotal() {
        CustomersTriggerItemServed.Instance instance = new CustomersTriggerItemServed.Instance(
                Optional.empty(),
                Optional.of(CustomerSpawnerMode.LUNCH),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.atLeast(100)),
                Optional.empty()
        );

        assertTrue(instance.matchesEvent(event(CustomerSpawnerMode.LUNCH, 3, 2, false)));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, false), 99, 0));
    }

    @Test
    void schemaDescribesAndUpdatesTriggerProperties() {
        assertEquals("spawner_location", CustomersTriggerItemServed.SCHEMA.properties().get(1).serializedName());
        assertEquals("spawner_mode", CustomersTriggerItemServed.SCHEMA.properties().get(2).serializedName());
        assertEquals(
                CustomersTriggerSchema.Editor.OPTIONAL_LOCATION,
                CustomersTriggerItemServed.SCHEMA.properties().get(1).editor()
        );
        CustomersTriggerSchema.Property<CustomersTriggerItemServed.Instance> spawnerMode =
                CustomersTriggerItemServed.SCHEMA.property("spawner_mode").orElseThrow();

        assertEquals("advancements.triggers.property.spawner_mode", spawnerMode.name().getString());
        assertEquals(CustomersTriggerSchema.Editor.OPTIONAL_ENUM, spawnerMode.editor());
        assertTrue(spawnerMode.taskEditable());

        CustomersTriggerItemServed.Instance updated = CustomersTriggerItemServed.SCHEMA.with(
                CustomersTriggerItemServed.Instance.ANY,
                spawnerMode,
                Optional.of(CustomerSpawnerMode.LUNCH)
        );

        assertEquals(Optional.of(CustomerSpawnerMode.LUNCH), updated.spawnerMode());
    }

    @Test
    void schemaKeepsAdvancementOnlyPropertiesOutOfTaskEditors() {
        assertFalse(CustomersTriggerItemServed.SCHEMA.property("player").orElseThrow().taskEditable());
        assertFalse(CustomersTriggerItemServed.SCHEMA.property("total_items_served").orElseThrow().taskEditable());
        assertFalse(
                CustomersTriggerItemServed.SCHEMA.property("total_pet_items_served").orElseThrow().taskEditable()
        );
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
                  "is_pet_item": true,
                  "total_items_served": { "min": 100 },
                  "total_pet_items_served": { "min": 25 }
                }
                """;

        CustomersTriggerItemServed.Instance instance = CustomersTriggerItemServed.Instance.CODEC
                .parse(
                        RegistryOps.create(
                                JsonOps.INSTANCE,
                                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                        ),
                        JsonParser.parseString(json)
                )
                .getOrThrow();

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, true), 100, 25));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, false), 100, 25));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 3, true), 100, 25));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, true), 99, 25));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3, 2, true), 100, 24));
    }

    private static CustomerInternalEvents.ItemServed event(
            CustomerSpawnerMode mode,
            int servedCount,
            int costCount,
            boolean isPetItem
    ) {
        return event(
                mock(ServerLevel.class),
                null,
                mode,
                servedCount,
                costCount,
                isPetItem
        );
    }

    private static CustomerInternalEvents.ItemServed event(
            ServerLevel level,
            BlockPos spawnerPosition,
            CustomerSpawnerMode mode,
            int servedCount,
            int costCount,
            boolean isPetItem
    ) {
        return new CustomerInternalEvents.ItemServed(
                level,
                spawnerPosition,
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
