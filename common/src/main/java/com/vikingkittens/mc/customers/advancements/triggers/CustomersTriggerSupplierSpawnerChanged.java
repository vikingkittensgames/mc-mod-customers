package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.supplier.SupplierInternalEvents;

public final class CustomersTriggerSupplierSpawnerChanged
        extends SimpleCriterionTrigger<CustomersTriggerSupplierSpawnerChanged.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, SupplierInternalEvents.SupplierSpawnerConfigChanged event) {
        trigger(player, instance -> instance.matchesEvent(event));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
            Optional<Boolean> autoCost,
            Optional<MinMaxBounds.Ints> numSellItems,
            Optional<MinMaxBounds.Ints> numCostItems,
            Optional<MinMaxBounds.Ints> numAppearances
    ) implements SimpleInstance {
        public static final Instance ANY = new Instance(
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
                                "autoCost",
                                "auto_cost",
                                Codec.BOOL,
                                CustomersTriggerSchema.Editor.OPTIONAL_BOOLEAN,
                                true
                        )
                        .property(
                                "numSellItems",
                                "num_sell_items",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numCostItems",
                                "num_cost_items",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "numAppearances",
                                "num_appearances",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matchesEvent(SupplierInternalEvents.SupplierSpawnerConfigChanged event) {
            return spawnerLocation
                            .map(value -> value.matches(event.level(), event.spawnerPosition()))
                            .orElse(true)
                    && autoCost.map(value -> value == event.autoCost()).orElse(true)
                    && numSellItems.map(value -> value.matches(event.numSellItems())).orElse(true)
                    && numCostItems.map(value -> value.matches(event.numCostItems())).orElse(true)
                    && numAppearances.map(value -> value.matches(event.numAppearances())).orElse(true);
        }
    }
}
