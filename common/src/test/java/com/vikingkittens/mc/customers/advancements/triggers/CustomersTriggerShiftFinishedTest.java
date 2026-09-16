package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersTriggerShiftFinishedTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesAnEventWhenAllOptionalConditionsAreAbsent() {
        CustomersTriggerShiftFinished.Instance instance = instance(
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

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3), 1));
    }

    @Test
    void matchesShiftConditionsTogether() {
        CustomersTriggerShiftFinished.Instance instance = instance(
                Optional.of(CustomerSpawnerMode.LUNCH),
                Optional.of(MinMaxBounds.Ints.exactly(3)),
                Optional.of(MinMaxBounds.Doubles.atLeast(0.75)),
                Optional.of(MinMaxBounds.Ints.exactly(10)),
                Optional.of(MinMaxBounds.Ints.exactly(8)),
                Optional.of(MinMaxBounds.Ints.exactly(2)),
                Optional.of(MinMaxBounds.Ints.exactly(20)),
                Optional.of(MinMaxBounds.Ints.exactly(4)),
                Optional.of(MinMaxBounds.Ints.exactly(2)),
                Optional.of(MinMaxBounds.Ints.exactly(2)),
                Optional.of(MinMaxBounds.Ints.atLeast(5))
        );

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3), 5));
        assertFalse(instance.matches(event(CustomerSpawnerMode.DINNER, 3), 5));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 2), 5));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3), 4));
    }

    @Test
    void matchesOneStarPercentageCriterion() {
        String json = """
                {
                  "percentage": { "min": 0.15 }
                }
                """;
        CustomersTriggerShiftFinished.Instance instance =
                CustomersTriggerShiftFinished.Instance.CODEC
                        .parse(registryOps(), JsonParser.parseString(json))
                        .getOrThrow();

        assertTrue(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 1, 0.15F),
                1
        ));
        assertTrue(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 1, 1.0F),
                1
        ));
        assertFalse(instance.matches(
                event(CustomerSpawnerMode.LUNCH, 1, 0.14F),
                1
        ));
    }

    @Test
    void decodesDataDrivenShiftFinishedConditions() {
        String json = """
                {
                  "spawner_mode": "lunch",
                  "level": 3,
                  "percentage": { "min": 0.75 },
                  "total_customers": 10,
                  "num_customers_served": 8,
                  "num_customers_gave_up": 2,
                  "total_items_wanted": 20,
                  "num_players": 4,
                  "num_crafters": 2,
                  "num_servers": 2,
                  "total_shifts_finished": { "min": 5 }
                }
                """;

        CustomersTriggerShiftFinished.Instance instance = CustomersTriggerShiftFinished.Instance.CODEC
                .parse(registryOps(), JsonParser.parseString(json))
                .getOrThrow();

        assertTrue(instance.matches(event(CustomerSpawnerMode.LUNCH, 3), 5));
        assertFalse(instance.matches(event(CustomerSpawnerMode.LUNCH, 3), 4));
    }

    @Test
    void encodesTotalCustomersFromTheMatchingField() {
        CustomersTriggerShiftFinished.Instance instance = instance(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(MinMaxBounds.Ints.exactly(10)),
                Optional.of(MinMaxBounds.Ints.exactly(8)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        JsonObject encoded = CustomersTriggerShiftFinished.Instance.CODEC
                .encodeStart(registryOps(), instance)
                .getOrThrow()
                .getAsJsonObject();

        assertEquals(10, encoded.get("total_customers").getAsInt());
        assertEquals(8, encoded.get("num_customers_served").getAsInt());
    }

    private static CustomersTriggerShiftFinished.Instance instance(
            Optional<CustomerSpawnerMode> spawnerMode,
            Optional<MinMaxBounds.Ints> activeLevel,
            Optional<MinMaxBounds.Doubles> percentage,
            Optional<MinMaxBounds.Ints> totalCustomers,
            Optional<MinMaxBounds.Ints> numCustomersServed,
            Optional<MinMaxBounds.Ints> numCustomersGaveUp,
            Optional<MinMaxBounds.Ints> totalItemsWanted,
            Optional<MinMaxBounds.Ints> numPlayers,
            Optional<MinMaxBounds.Ints> numCrafters,
            Optional<MinMaxBounds.Ints> numServers,
            Optional<MinMaxBounds.Ints> totalShiftsFinished
    ) {
        return new CustomersTriggerShiftFinished.Instance(
                Optional.empty(),
                spawnerMode,
                activeLevel,
                percentage,
                totalCustomers,
                numCustomersServed,
                numCustomersGaveUp,
                totalItemsWanted,
                numPlayers,
                numCrafters,
                numServers,
                totalShiftsFinished
        );
    }

    private static CustomerInternalEvents.ShiftFinished event(CustomerSpawnerMode mode, int activeLevel) {
        return event(mode, activeLevel, 0.75F);
    }

    private static CustomerInternalEvents.ShiftFinished event(
            CustomerSpawnerMode mode,
            int activeLevel,
            float percentage
    ) {
        return new CustomerInternalEvents.ShiftFinished(
                mock(ServerLevel.class),
                null,
                mode,
                activeLevel,
                percentage,
                10,
                8,
                2,
                20,
                Map.of(UUID.randomUUID(), 3, UUID.randomUUID(), 2),
                Map.of(UUID.randomUUID(), 5, UUID.randomUUID(), 3),
                0,
                0
        );
    }

    private static RegistryOps<com.google.gson.JsonElement> registryOps() {
        return RegistryOps.create(
                JsonOps.INSTANCE,
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
        );
    }
}
