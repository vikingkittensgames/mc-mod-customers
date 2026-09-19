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

class CustomersTriggerSuppliesPurchasedTest {
    private static final BlockPos SPAWNER_POSITION = new BlockPos(-120, 64, 350);

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesAnEventWhenAllOptionalConditionsAreAbsent() {
        assertTrue(CustomersTriggerSuppliesPurchased.Instance.ANY.matches(event(3, 2), 1));
    }

    @Test
    void matchesStackCountsLocationAndLifetimeTotalTogether() {
        CustomersTriggerSuppliesPurchased.Instance instance = new CustomersTriggerSuppliesPurchased.Instance(
                Optional.empty(),
                Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, SPAWNER_POSITION)),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.exactly(3)),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.exactly(2)),
                Optional.of(MinMaxBounds.Ints.atLeast(100))
        );

        assertTrue(instance.matches(event(3, 2), 100));
        assertFalse(instance.matches(event(2, 2), 100));
        assertFalse(instance.matches(event(3, 1), 100));
        assertFalse(instance.matches(event(3, 2), 99));
        assertFalse(instance.matches(event(Level.NETHER, SPAWNER_POSITION, 3, 2), 100));
    }

    @Test
    void eventMatchingDoesNotDependOnThePlayerLifetimeTotal() {
        CustomersTriggerSuppliesPurchased.Instance instance = new CustomersTriggerSuppliesPurchased.Instance(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.atLeast(100))
        );

        assertTrue(instance.matchesEvent(event(3, 2)));
        assertFalse(instance.matches(event(3, 2), 99));
    }

    @Test
    void schemaKeepsLifetimeTotalOutOfTaskEditors() {
        assertEquals("spawner_location", CustomersTriggerSuppliesPurchased.SCHEMA.properties().get(1).serializedName());
        assertEquals("supply_item", CustomersTriggerSuppliesPurchased.SCHEMA.properties().get(2).serializedName());
        assertFalse(CustomersTriggerSuppliesPurchased.SCHEMA.property("player").orElseThrow().taskEditable());
        assertFalse(
                CustomersTriggerSuppliesPurchased.SCHEMA
                        .property("total_supplies_purchased")
                        .orElseThrow()
                        .taskEditable()
        );
    }

    @Test
    void decodesDataDrivenSupplyAndCostConditions() {
        String json = """
                {
                  "supply_item": { "items": ["minecraft:bread"] },
                  "supply_count": { "min": 3 },
                  "cost_item": { "items": ["minecraft:emerald"] },
                  "cost_count": 2,
                  "total_supplies_purchased": { "min": 100 }
                }
                """;

        CustomersTriggerSuppliesPurchased.Instance instance = CustomersTriggerSuppliesPurchased.Instance.CODEC
                .parse(
                        RegistryOps.create(
                                JsonOps.INSTANCE,
                                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                        ),
                        JsonParser.parseString(json)
                )
                .getOrThrow();

        assertTrue(instance.matches(event(3, 2), 100));
        assertFalse(instance.matches(event(3, 1), 100));
    }

    private static SupplierInternalEvents.SuppliesPurchased event(int supplyCount, int costCount) {
        return event(Level.OVERWORLD, SPAWNER_POSITION, supplyCount, costCount);
    }

    private static SupplierInternalEvents.SuppliesPurchased event(
            net.minecraft.resources.ResourceKey<Level> dimension,
            BlockPos spawnerPosition,
            int supplyCount,
            int costCount
    ) {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(dimension);
        return new SupplierInternalEvents.SuppliesPurchased(
                level,
                spawnerPosition,
                UUID.randomUUID(),
                new ItemStack(Items.BREAD, supplyCount),
                new ItemStack(Items.EMERALD, costCount)
        );
    }
}
