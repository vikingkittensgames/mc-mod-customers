package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public final class CustomersTriggerLeaderboardChanged
        extends SimpleCriterionTrigger<CustomersTriggerLeaderboardChanged.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, CustomerInternalEvents.LeaderboardChanged event) {
        trigger(player, instance -> instance.matchesEvent(event, player.getUUID()));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
            Optional<CustomerSpawnerMode> spawnerMode,
            Optional<CustomersLocationPredicate> leaderboardLocation,
            Optional<MinMaxBounds.Ints> activeLevel,
            Optional<MinMaxBounds.Doubles> previousScore,
            Optional<MinMaxBounds.Doubles> newScore,
            Optional<Boolean> isLeader,
            Optional<Boolean> wasLeader,
            Optional<Boolean> leaderChanged
    ) implements SimpleInstance {
        public static final Instance ANY = new Instance(
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

        public static final CustomersTriggerSchema<Instance> SCHEMA =
                CustomersTriggerSchema.builder(Instance.class)
                        .property(
                                "player",
                                "player",
                                ContextAwarePredicate.CODEC,
                                CustomersTriggerSchema.Editor.HIDDEN,
                                false
                        )
                        .property(
                                "spawnerLocation",
                                "spawner_location",
                                CustomersLocationPredicate.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_LOCATION,
                                true
                        )
                        .property(
                                "spawnerMode",
                                "spawner_mode",
                                StringRepresentable.fromEnum(CustomerSpawnerMode::values),
                                CustomersTriggerSchema.Editor.OPTIONAL_ENUM,
                                List.of(CustomerSpawnerMode.values()),
                                true
                        )
                        .property(
                                "leaderboardLocation",
                                "leaderboard_location",
                                CustomersLocationPredicate.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_LOCATION,
                                true
                        )
                        .property(
                                "activeLevel",
                                "level",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "previousScore",
                                "previous_score",
                                MinMaxBounds.Doubles.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_DOUBLE_RANGE,
                                true
                        )
                        .property(
                                "newScore",
                                "new_score",
                                MinMaxBounds.Doubles.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_DOUBLE_RANGE,
                                true
                        )
                        .property(
                                "isLeader",
                                "is_leader",
                                Codec.BOOL,
                                CustomersTriggerSchema.Editor.OPTIONAL_BOOLEAN,
                                true
                        )
                        .property(
                                "wasLeader",
                                "was_leader",
                                Codec.BOOL,
                                CustomersTriggerSchema.Editor.OPTIONAL_BOOLEAN,
                                true
                        )
                        .property(
                                "leaderChanged",
                                "leader_changed",
                                Codec.BOOL,
                                CustomersTriggerSchema.Editor.OPTIONAL_BOOLEAN,
                                true
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matchesEvent(CustomerInternalEvents.LeaderboardChanged event, UUID playerId) {
            Float previousPlayerScore = event.previousScores().get(playerId);
            Float newPlayerScore = event.scores().get(playerId);
            return spawnerLocation.map(value -> value.matches(event)).orElse(true)
                    && spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && leaderboardLocation
                            .map(value -> value.matches(event.level(), event.leaderboardPosition()))
                            .orElse(true)
                    && activeLevel.map(value -> value.matches(event.activeLevel())).orElse(true)
                    && previousScore
                            .map(value -> previousPlayerScore != null && value.matches(previousPlayerScore.doubleValue()))
                            .orElse(true)
                    && newScore
                            .map(value -> newPlayerScore != null && value.matches(newPlayerScore.doubleValue()))
                            .orElse(true)
                    && isLeader.map(value -> value == playerId.equals(event.leader())).orElse(true)
                    && wasLeader.map(value -> value == playerId.equals(event.previousLeader())).orElse(true)
                    && leaderChanged.map(value -> value == event.leaderChanged()).orElse(true);
        }
    }
}
