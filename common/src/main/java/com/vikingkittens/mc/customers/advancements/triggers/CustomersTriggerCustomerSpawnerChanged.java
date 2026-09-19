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

public final class CustomersTriggerCustomerSpawnerChanged
        extends SimpleCriterionTrigger<CustomersTriggerCustomerSpawnerChanged.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, CustomerInternalEvents.CustomerSpawnerConfigChanged event) {
        trigger(player, instance -> instance.matchesEvent(event));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
            Optional<CustomerSpawnerMode> spawnerMode,
            Optional<MinMaxBounds.Ints> activeLevel,
            Optional<MinMaxBounds.Doubles> requiredStars,
            Optional<MinMaxBounds.Ints> maxCustomers,
            Optional<MinMaxBounds.Doubles> petPercentage,
            Optional<Boolean> petTypesCustomized,
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
                                "requiredStars",
                                "required_stars",
                                MinMaxBounds.Doubles.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_DOUBLE_RANGE,
                                true
                        )
                        .property(
                                "maxCustomers",
                                "max_customers",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "petPercentage",
                                "pet_percentage",
                                MinMaxBounds.Doubles.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_DOUBLE_RANGE,
                                true
                        )
                        .property(
                                "petTypesCustomized",
                                "pet_types_customized",
                                Codec.BOOL,
                                CustomersTriggerSchema.Editor.OPTIONAL_BOOLEAN,
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

        public boolean matchesEvent(CustomerInternalEvents.CustomerSpawnerConfigChanged event) {
            return spawnerLocation
                            .map(value -> value.matches(event.level(), event.spawnerPosition()))
                            .orElse(true)
                    && spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && activeLevel.map(value -> value.matches(event.activeLevel())).orElse(true)
                    && requiredStars.map(value -> value.matches(event.requiredStars())).orElse(true)
                    && maxCustomers.map(value -> value.matches(event.maxCustomers())).orElse(true)
                    && petPercentage.map(value -> value.matches(event.petPercentage())).orElse(true)
                    && petTypesCustomized.map(value -> value == event.petTypesCustomized()).orElse(true)
                    && autoCost.map(value -> value == event.autoCost()).orElse(true)
                    && numSellItems.map(value -> value.matches(event.numSellItems())).orElse(true)
                    && numCostItems.map(value -> value.matches(event.numCostItems())).orElse(true)
                    && numAppearances.map(value -> value.matches(event.numAppearances())).orElse(true);
        }
    }
}
