package com.vikingkittens.mc.customers.advancements.triggers;

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
import com.vikingkittens.mc.customers.supplier.SupplierInternalEvents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersTriggerSupplierSpawnerChangedTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesEverySimpleSupplierConfigurationProperty() {
        CustomersTriggerSupplierSpawnerChanged.Instance instance =
                new CustomersTriggerSupplierSpawnerChanged.Instance(
                        Optional.empty(),
                        Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, BlockPos.ZERO)),
                        Optional.of(true),
                        Optional.of(MinMaxBounds.Ints.exactly(2)),
                        Optional.of(MinMaxBounds.Ints.exactly(1)),
                        Optional.of(MinMaxBounds.Ints.exactly(2))
                );

        assertTrue(instance.matchesEvent(event()));
        assertFalse(CustomersTriggerSupplierSpawnerChanged.SCHEMA.with(
                        instance,
                        CustomersTriggerSupplierSpawnerChanged.SCHEMA.property("auto_cost").orElseThrow(),
                        Optional.of(false)
                )
                .matchesEvent(event()));
    }

    @Test
    void schemaExposesOnlySimpleEventPropertiesToTasks() {
        assertEquals(
                List.of(
                        "player",
                        "spawner_location",
                        "auto_cost",
                        "num_sell_items",
                        "num_cost_items",
                        "num_appearances"
                ),
                CustomersTriggerSupplierSpawnerChanged.SCHEMA.properties().stream()
                        .map(CustomersTriggerSchema.Property::serializedName)
                        .toList()
        );
        assertFalse(CustomersTriggerSupplierSpawnerChanged.SCHEMA.property("player").orElseThrow().taskEditable());
        assertTrue(CustomersTriggerSupplierSpawnerChanged.SCHEMA.properties().stream()
                .filter(property -> !property.serializedName().equals("player"))
                .allMatch(CustomersTriggerSchema.Property::taskEditable));
    }

    @Test
    void decodesDataDrivenSupplierConfigurationConditions() {
        String json = """
                {
                  "auto_cost": true,
                  "num_sell_items": 2,
                  "num_cost_items": 1,
                  "num_appearances": 2
                }
                """;

        CustomersTriggerSupplierSpawnerChanged.Instance instance =
                CustomersTriggerSupplierSpawnerChanged.Instance.CODEC
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

    private static SupplierInternalEvents.SupplierSpawnerConfigChanged event() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return new SupplierInternalEvents.SupplierSpawnerConfigChanged(
                level,
                BlockPos.ZERO,
                UUID.randomUUID(),
                List.of(
                        new SupplierInternalEvents.Offer(
                                new ItemStack(Items.APPLE),
                                new ItemStack(Items.EMERALD)
                        ),
                        new SupplierInternalEvents.Offer(new ItemStack(Items.CARROT), ItemStack.EMPTY)
                ),
                true,
                List.of("customers:first", "customers:second")
        );
    }
}
