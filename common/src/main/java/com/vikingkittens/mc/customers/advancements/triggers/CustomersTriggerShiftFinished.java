package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public final class CustomersTriggerShiftFinished
        extends SimpleCriterionTrigger<CustomersTriggerShiftFinished.Instance> {
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
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                StringRepresentable.fromEnum(CustomerSpawnerMode::values)
                        .optionalFieldOf("spawner_mode")
                        .forGetter(Instance::spawnerMode),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("level").forGetter(Instance::activeLevel),
                MinMaxBounds.Doubles.CODEC.optionalFieldOf("percentage").forGetter(Instance::percentage),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("total_customers").forGetter(Instance::totalCustomers),
                MinMaxBounds.Ints.CODEC
                        .optionalFieldOf("num_customers_served")
                        .forGetter(Instance::numCustomersServed),
                MinMaxBounds.Ints.CODEC
                        .optionalFieldOf("num_customers_gave_up")
                        .forGetter(Instance::numCustomersGaveUp),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("total_items_wanted").forGetter(Instance::totalItemsWanted),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("num_players").forGetter(Instance::numPlayers),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("num_crafters").forGetter(Instance::numCrafters),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("num_servers").forGetter(Instance::numServers),
                MinMaxBounds.Ints.CODEC
                        .optionalFieldOf("total_shifts_finished")
                        .forGetter(Instance::totalShiftsFinished)
        ).apply(instance, Instance::new));

        public boolean matches(
                CustomerInternalEvents.ShiftFinished event,
                int currentTotalShiftsFinished
        ) {
            return spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
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
                    && numServers.map(value -> value.matches(event.playerItemsServed().size())).orElse(true)
                    && totalShiftsFinished.map(value -> value.matches(currentTotalShiftsFinished)).orElse(true);
        }
    }
}
