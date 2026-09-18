package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

class CustomersTriggerCustomerSpawnerChangedTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesEverySimpleCustomerConfigurationProperty() {
        CustomersTriggerCustomerSpawnerChanged.Instance instance =
                new CustomersTriggerCustomerSpawnerChanged.Instance(
                        Optional.empty(),
                        Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, BlockPos.ZERO)),
                        Optional.of(CustomerSpawnerMode.LUNCH),
                        Optional.of(MinMaxBounds.Ints.exactly(2)),
                        Optional.of(MinMaxBounds.Doubles.exactly(4.5D)),
                        Optional.of(MinMaxBounds.Ints.exactly(12)),
                        Optional.of(MinMaxBounds.Doubles.exactly(0.25D)),
                        Optional.of(true),
                        Optional.of(false),
                        Optional.of(MinMaxBounds.Ints.exactly(2)),
                        Optional.of(MinMaxBounds.Ints.exactly(1)),
                        Optional.of(MinMaxBounds.Ints.exactly(2))
                );

        assertTrue(instance.matchesEvent(event()));
        assertFalse(CustomersTriggerCustomerSpawnerChanged.SCHEMA.with(
                        instance,
                        CustomersTriggerCustomerSpawnerChanged.SCHEMA.property("max_customers").orElseThrow(),
                        Optional.of(MinMaxBounds.Ints.exactly(13))
                )
                .matchesEvent(event()));
    }

    @Test
    void schemaExposesOnlySimpleEventPropertiesToTasks() {
        assertEquals(
                List.of(
                        "player",
                        "spawner_location",
                        "spawner_mode",
                        "level",
                        "required_stars",
                        "max_customers",
                        "pet_percentage",
                        "pet_types_customized",
                        "auto_cost",
                        "num_sell_items",
                        "num_cost_items",
                        "num_appearances"
                ),
                CustomersTriggerCustomerSpawnerChanged.SCHEMA.properties().stream()
                        .map(CustomersTriggerSchema.Property::serializedName)
                        .toList()
        );
        assertFalse(CustomersTriggerCustomerSpawnerChanged.SCHEMA.property("player").orElseThrow().taskEditable());
        assertTrue(CustomersTriggerCustomerSpawnerChanged.SCHEMA.properties().stream()
                .filter(property -> !property.serializedName().equals("player"))
                .allMatch(CustomersTriggerSchema.Property::taskEditable));
    }

    @Test
    void decodesDataDrivenCustomerConfigurationConditions() {
        String json = """
                {
                  "spawner_mode": "lunch",
                  "level": 2,
                  "required_stars": 4.5,
                  "max_customers": 12,
                  "pet_percentage": 0.25,
                  "pet_types_customized": true,
                  "auto_cost": false,
                  "num_sell_items": 2,
                  "num_cost_items": 1,
                  "num_appearances": 2
                }
                """;

        CustomersTriggerCustomerSpawnerChanged.Instance instance =
                CustomersTriggerCustomerSpawnerChanged.Instance.CODEC
                        .parse(
                                RegistryOps.create(
                                        JsonOps.INSTANCE,
                                        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                                ),
                                JsonParser.parseString(json)
                        )
                        .getOrThrow();

        assertTrue(instance.matchesEvent(event()));
    }

    private static CustomerInternalEvents.CustomerSpawnerConfigChanged event() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return new CustomerInternalEvents.CustomerSpawnerConfigChanged(
                level,
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                2,
                List.of(List.of(new ItemStack(Items.APPLE), new ItemStack(Items.CARROT))),
                List.of(new ItemStack(Items.EMERALD)),
                4.5F,
                12,
                0.25F,
                true,
                new LinkedHashSet<>(List.of("minecraft:cat")),
                new LinkedHashMap<>(),
                false,
                List.of("customers:first", "customers:second")
        );
    }
}
