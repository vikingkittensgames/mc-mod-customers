package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.List;
import java.util.Map;
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
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersTriggerLeaderboardChangedTest {
    private static final UUID PREVIOUS_LEADER = UUID.randomUUID();
    private static final UUID NEW_LEADER = UUID.randomUUID();

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesLocationsLevelPlayerScoresAndLeaderChanges() {
        CustomersTriggerLeaderboardChanged.Instance instance = new CustomersTriggerLeaderboardChanged.Instance(
                Optional.empty(),
                Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, BlockPos.ZERO)),
                Optional.of(CustomerSpawnerMode.LUNCH),
                Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, new BlockPos(8, 64, 8))),
                Optional.of(MinMaxBounds.Ints.exactly(1)),
                Optional.of(MinMaxBounds.Doubles.between(0.39D, 0.41D)),
                Optional.of(MinMaxBounds.Doubles.atLeast(0.75D)),
                Optional.of(true),
                Optional.of(false),
                Optional.of(true)
        );

        assertTrue(instance.matchesEvent(event(), NEW_LEADER));
        assertFalse(instance.matchesEvent(event(), PREVIOUS_LEADER));
    }

    @Test
    void previousScoreConditionDoesNotMatchAPlayersFirstScore() {
        CustomersTriggerLeaderboardChanged.Instance instance = CustomersTriggerLeaderboardChanged.SCHEMA.with(
                CustomersTriggerLeaderboardChanged.Instance.ANY,
                CustomersTriggerLeaderboardChanged.SCHEMA.property("previous_score").orElseThrow(),
                Optional.of(MinMaxBounds.Doubles.atLeast(0.0D))
        );

        assertFalse(instance.matchesEvent(firstScoreEvent(), NEW_LEADER));
    }

    @Test
    void schemaExposesEveryEventConditionToTasksExceptTheAdvancementPlayerPredicate() {
        assertEquals(
                List.of(
                        "player",
                        "spawner_location",
                        "spawner_mode",
                        "leaderboard_location",
                        "level",
                        "previous_score",
                        "new_score",
                        "is_leader",
                        "was_leader",
                        "leader_changed"
                ),
                CustomersTriggerLeaderboardChanged.SCHEMA.properties().stream()
                        .map(CustomersTriggerSchema.Property::serializedName)
                        .toList()
        );
        assertFalse(CustomersTriggerLeaderboardChanged.SCHEMA.property("player").orElseThrow().taskEditable());
        assertTrue(CustomersTriggerLeaderboardChanged.SCHEMA.properties().stream()
                .filter(property -> !property.serializedName().equals("player"))
                .allMatch(CustomersTriggerSchema.Property::taskEditable));
    }

    @Test
    void decodesDataDrivenLeaderboardConditions() {
        String json = """
                {
                  "spawner_mode": "lunch",
                  "level": 1,
                  "previous_score": { "min": 0.4 },
                  "new_score": { "min": 0.75 },
                  "is_leader": true,
                  "was_leader": false,
                  "leader_changed": true
                }
                """;

        CustomersTriggerLeaderboardChanged.Instance instance =
                CustomersTriggerLeaderboardChanged.Instance.CODEC
                        .parse(
                                RegistryOps.create(
                                        JsonOps.INSTANCE,
                                        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                                ),
                                JsonParser.parseString(json)
                        )
                        .getOrThrow();

        assertTrue(instance.matchesEvent(event(), NEW_LEADER));
    }

    private static CustomerInternalEvents.LeaderboardChanged event() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return new CustomerInternalEvents.LeaderboardChanged(
                level,
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                new BlockPos(8, 64, 8),
                1,
                Map.of(PREVIOUS_LEADER, 0.5F, NEW_LEADER, 0.75F),
                Map.of(PREVIOUS_LEADER, 0.5F, NEW_LEADER, 0.4F)
        );
    }

    private static CustomerInternalEvents.LeaderboardChanged firstScoreEvent() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return new CustomerInternalEvents.LeaderboardChanged(
                level,
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                new BlockPos(8, 64, 8),
                1,
                Map.of(PREVIOUS_LEADER, 0.5F, NEW_LEADER, 0.75F),
                Map.of(PREVIOUS_LEADER, 0.5F)
        );
    }
}
