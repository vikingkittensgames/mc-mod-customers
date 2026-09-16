package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public final class CustomersTriggerCustomerServed
        extends SimpleCriterionTrigger<CustomersTriggerCustomerServed.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(
            ServerPlayer player,
            CustomerInternalEvents.CustomerServed event,
            int totalCustomersServed,
            int totalCustomersCasualServed,
            int totalCustomersNormalServed,
            int totalCustomersImpatientServed
    ) {
        trigger(player, instance -> instance.matches(
                event,
                totalCustomersServed,
                totalCustomersCasualServed,
                totalCustomersNormalServed,
                totalCustomersImpatientServed
        ));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
            Optional<CustomerSpawnerMode> spawnerMode,
            Optional<ResourceLocation> customerProfession,
            Optional<ItemPredicate> servedItem,
            Optional<MinMaxBounds.Ints> servedCount,
            Optional<ItemPredicate> costItem,
            Optional<MinMaxBounds.Ints> costCount,
            Optional<Boolean> isPetItem,
            Optional<MinMaxBounds.Ints> totalCustomersServed,
            Optional<MinMaxBounds.Ints> totalCustomersCasualServed,
            Optional<MinMaxBounds.Ints> totalCustomersNormalServed,
            Optional<MinMaxBounds.Ints> totalCustomersImpatientServed
    ) implements SimpleInstance {
        public Instance(
                Optional<ContextAwarePredicate> player,
                Optional<CustomerSpawnerMode> spawnerMode,
                Optional<ResourceLocation> customerProfession,
                Optional<ItemPredicate> servedItem,
                Optional<MinMaxBounds.Ints> servedCount,
                Optional<ItemPredicate> costItem,
                Optional<MinMaxBounds.Ints> costCount,
                Optional<Boolean> isPetItem,
                Optional<MinMaxBounds.Ints> totalCustomersServed,
                Optional<MinMaxBounds.Ints> totalCustomersCasualServed,
                Optional<MinMaxBounds.Ints> totalCustomersNormalServed,
                Optional<MinMaxBounds.Ints> totalCustomersImpatientServed
        ) {
            this(
                    player,
                    Optional.empty(),
                    spawnerMode,
                    customerProfession,
                    servedItem,
                    servedCount,
                    costItem,
                    costCount,
                    isPetItem,
                    totalCustomersServed,
                    totalCustomersCasualServed,
                    totalCustomersNormalServed,
                    totalCustomersImpatientServed
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
                                "customerProfession",
                                "customer_profession",
                                ResourceLocation.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_RESOURCE_LOCATION,
                                true
                        )
                        .property(
                                "servedItem",
                                "served_item",
                                ItemPredicate.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_ITEM_PREDICATE,
                                true
                        )
                        .property(
                                "servedCount",
                                "served_count",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "costItem",
                                "cost_item",
                                ItemPredicate.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_ITEM_PREDICATE,
                                true
                        )
                        .property(
                                "costCount",
                                "cost_count",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                true
                        )
                        .property(
                                "isPetItem",
                                "is_pet_item",
                                Codec.BOOL,
                                CustomersTriggerSchema.Editor.OPTIONAL_BOOLEAN,
                                true
                        )
                        .property(
                                "totalCustomersServed",
                                "total_customers_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .property(
                                "totalCustomersCasualServed",
                                "total_casual_customers_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .property(
                                "totalCustomersNormalServed",
                                "total_normal_customers_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .property(
                                "totalCustomersImpatientServed",
                                "total_impatient_customers_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matches(
                CustomerInternalEvents.CustomerServed event,
                int currentTotalCustomersServed,
                int currentTotalCustomersCasualServed,
                int currentTotalCustomersNormalServed,
                int currentTotalCustomersImpatientServed
        ) {
            return matchesEvent(event)
                    && totalCustomersServed.map(value -> value.matches(currentTotalCustomersServed)).orElse(true)
                    && totalCustomersCasualServed
                            .map(value -> value.matches(currentTotalCustomersCasualServed))
                            .orElse(true)
                    && totalCustomersNormalServed
                            .map(value -> value.matches(currentTotalCustomersNormalServed))
                            .orElse(true)
                    && totalCustomersImpatientServed
                            .map(value -> value.matches(currentTotalCustomersImpatientServed))
                            .orElse(true);
        }

        public boolean matchesEvent(CustomerInternalEvents.CustomerServed event) {
            return spawnerLocation.map(value -> value.matches(event)).orElse(true)
                    && spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && customerProfession.map(value -> value.equals(event.customerProfession())).orElse(true)
                    && servedItem.map(value -> value.test(event.servedItem())).orElse(true)
                    && servedCount.map(value -> value.matches(event.servedItem().getCount())).orElse(true)
                    && costItem.map(value -> value.test(event.costItem())).orElse(true)
                    && costCount.map(value -> value.matches(event.costItem().getCount())).orElse(true)
                    && isPetItem.map(value -> value == event.isPetItem()).orElse(true);
        }
    }
}
