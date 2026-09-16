package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public final class CustomersTriggerShiftFinished
        extends SimpleCriterionTrigger<CustomersTriggerShiftFinished.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(
            ServerPlayer player,
            CustomerInternalEvents.ShiftFinished event,
            int totalShiftsFinished
    ) {
        trigger(player, instance -> instance.matches(event, totalShiftsFinished));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
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
    ) implements SimpleInstance {
        public Instance(
                Optional<ContextAwarePredicate> player,
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
            this(
                    player,
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
                                "activeLevel",
                                "level",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "percentage",
                                "percentage",
                                MinMaxBounds.Doubles.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_DOUBLE_RANGE,
                                true
                        )
                        .property(
                                "totalCustomers",
                                "total_customers",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numCustomersServed",
                                "num_customers_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numCustomersGaveUp",
                                "num_customers_gave_up",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "totalItemsWanted",
                                "total_items_wanted",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numPlayers",
                                "num_players",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numCrafters",
                                "num_crafters",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numServers",
                                "num_servers",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "totalShiftsFinished",
                                "total_shifts_finished",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matches(
                CustomerInternalEvents.ShiftFinished event,
                int currentTotalShiftsFinished
        ) {
            return matchesEvent(event)
                    && totalShiftsFinished.map(value -> value.matches(currentTotalShiftsFinished)).orElse(true);
        }

        public boolean matchesEvent(CustomerInternalEvents.ShiftFinished event) {
            return spawnerLocation.map(value -> value.matches(event)).orElse(true)
                    && spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && activeLevel.map(value -> value.matches(event.activeLevel())).orElse(true)
                    && percentage.map(value -> value.matches(event.percentage())).orElse(true)
                    && totalCustomers.map(value -> value.matches(event.totalCustomers())).orElse(true)
                    && numCustomersServed.map(value -> value.matches(event.numCustomersServed())).orElse(true)
                    && numCustomersGaveUp.map(value -> value.matches(event.numCustomersGaveUp())).orElse(true)
                    && totalItemsWanted.map(value -> value.matches(event.totalItemsWanted())).orElse(true)
                    && numPlayers
                            .map(value -> value.matches(
                                    event.playerItemsCrafted().size() + event.playerItemsServed().size()
                            ))
                            .orElse(true)
                    && numCrafters.map(value -> value.matches(event.playerItemsCrafted().size())).orElse(true)
                    && numServers.map(value -> value.matches(event.playerItemsServed().size())).orElse(true);
        }
    }
}
