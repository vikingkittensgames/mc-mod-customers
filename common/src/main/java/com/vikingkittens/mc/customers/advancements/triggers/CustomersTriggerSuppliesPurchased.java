package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.supplier.SupplierInternalEvents;

public final class CustomersTriggerSuppliesPurchased
        extends SimpleCriterionTrigger<CustomersTriggerSuppliesPurchased.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(
            ServerPlayer player,
            SupplierInternalEvents.SuppliesPurchased event,
            int totalSuppliesPurchased
    ) {
        trigger(player, instance -> instance.matches(event, totalSuppliesPurchased));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
            Optional<ItemPredicate> supplyItem,
            Optional<MinMaxBounds.Ints> supplyCount,
            Optional<ItemPredicate> costItem,
            Optional<MinMaxBounds.Ints> costCount,
            Optional<MinMaxBounds.Ints> totalSuppliesPurchased
    ) implements SimpleInstance {
        public Instance(
                Optional<ContextAwarePredicate> player,
                Optional<ItemPredicate> supplyItem,
                Optional<MinMaxBounds.Ints> supplyCount,
                Optional<ItemPredicate> costItem,
                Optional<MinMaxBounds.Ints> costCount,
                Optional<MinMaxBounds.Ints> totalSuppliesPurchased
        ) {
            this(
                    player,
                    Optional.empty(),
                    supplyItem,
                    supplyCount,
                    costItem,
                    costCount,
                    totalSuppliesPurchased
            );
        }

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
                                "supplyItem",
                                "supply_item",
                                ItemPredicate.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_ITEM_PREDICATE,
                                true
                        )
                        .property(
                                "supplyCount",
                                "supply_count",
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
                                "totalSuppliesPurchased",
                                "total_supplies_purchased",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matches(
                SupplierInternalEvents.SuppliesPurchased event,
                int currentTotalSuppliesPurchased
        ) {
            return matchesEvent(event)
                    && totalSuppliesPurchased.map(value -> value.matches(currentTotalSuppliesPurchased)).orElse(true);
        }

        public boolean matchesEvent(SupplierInternalEvents.SuppliesPurchased event) {
            return spawnerLocation
                            .map(value -> value.matches(event.level(), event.spawnerPosition()))
                            .orElse(true)
                    && supplyItem.map(value -> value.test(event.supplyItem())).orElse(true)
                    && supplyCount.map(value -> value.matches(event.supplyItem().getCount())).orElse(true)
                    && costItem.map(value -> value.test(event.costItem())).orElse(true)
                    && costCount.map(value -> value.matches(event.costItem().getCount())).orElse(true);
        }
    }
}
