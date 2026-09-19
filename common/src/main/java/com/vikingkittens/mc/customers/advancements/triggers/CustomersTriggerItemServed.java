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

public final class CustomersTriggerItemServed
        extends SimpleCriterionTrigger<CustomersTriggerItemServed.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(
            ServerPlayer player,
            CustomerInternalEvents.ItemServed event,
            int totalItemsServed,
            int totalPetItemsServed
    ) {
        trigger(player, instance -> instance.matches(event, totalItemsServed, totalPetItemsServed));
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
            Optional<MinMaxBounds.Ints> totalItemsServed,
            Optional<MinMaxBounds.Ints> totalPetItemsServed
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
                Optional<MinMaxBounds.Ints> totalItemsServed,
                Optional<MinMaxBounds.Ints> totalPetItemsServed
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
                    totalItemsServed,
                    totalPetItemsServed
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
                                "totalItemsServed",
                                "total_items_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .property(
                                "totalPetItemsServed",
                                "total_pet_items_served",
                                MinMaxBounds.Ints.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_INT_RANGE,
                                false
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matches(
                CustomerInternalEvents.ItemServed event,
                int currentTotalItemsServed,
                int currentTotalPetItemsServed
        ) {
            return matchesEvent(event)
                    && totalItemsServed.map(value -> value.matches(currentTotalItemsServed)).orElse(true)
                    && totalPetItemsServed
                            .map(value -> value.matches(currentTotalPetItemsServed))
                            .orElse(true);
        }

        public boolean matchesEvent(CustomerInternalEvents.ItemServed event) {
            return spawnerLocation
                            .map(value -> value.matches(event.level(), event.spawnerPosition()))
                            .orElse(true)
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
