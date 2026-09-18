package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public final class CustomersTriggerCounterPlaced
        extends SimpleCriterionTrigger<CustomersTriggerCounterPlaced.Instance> {
    public static final CustomersTriggerSchema<Instance> SCHEMA = Instance.SCHEMA;

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, CustomerInternalEvents.CounterBlockPlaced event) {
        trigger(player, instance -> instance.matchesEvent(event));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomersLocationPredicate> spawnerLocation,
            Optional<CustomerSpawnerMode> spawnerMode,
            Optional<CustomersLocationPredicate> counterLocation,
            Optional<ResourceLocation> counterBlock
    ) implements SimpleInstance {
        public static final Instance ANY = new Instance(
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
                                "counterLocation",
                                "counter_location",
                                CustomersLocationPredicate.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_LOCATION,
                                true
                        )
                        .property(
                                "counterBlock",
                                "counter_block",
                                ResourceLocation.CODEC,
                                CustomersTriggerSchema.Editor.OPTIONAL_RESOURCE_LOCATION,
                                true
                        )
                        .build();

        public static final Codec<Instance> CODEC = SCHEMA.codec();

        public boolean matchesEvent(CustomerInternalEvents.CounterBlockPlaced event) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.counterBlockState().getBlock());
            return spawnerLocation.map(value -> value.matches(event)).orElse(true)
                    && spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && counterLocation
                            .map(value -> value.matches(event.level(), event.counterBlockPosition()))
                            .orElse(true)
                    && counterBlock.map(value -> value.equals(blockId)).orElse(true);
        }
    }
}
