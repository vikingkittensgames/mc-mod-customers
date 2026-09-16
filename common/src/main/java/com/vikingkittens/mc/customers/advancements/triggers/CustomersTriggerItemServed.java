package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(
            ServerPlayer player,
            CustomerInternalEvents.ItemServed event,
            int totalItemsServed
    ) {
        trigger(player, instance -> instance.matches(event, totalItemsServed));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<CustomerSpawnerMode> spawnerMode,
            Optional<ResourceLocation> customerProfession,
            Optional<ItemPredicate> servedItem,
            Optional<MinMaxBounds.Ints> servedCount,
            Optional<ItemPredicate> costItem,
            Optional<MinMaxBounds.Ints> costCount,
            Optional<Boolean> isPetItem,
            Optional<MinMaxBounds.Ints> totalItemsServed
    ) implements SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                StringRepresentable.fromEnum(CustomerSpawnerMode::values)
                        .optionalFieldOf("spawner_mode")
                        .forGetter(Instance::spawnerMode),
                ResourceLocation.CODEC.optionalFieldOf("customer_profession").forGetter(Instance::customerProfession),
                ItemPredicate.CODEC.optionalFieldOf("served_item").forGetter(Instance::servedItem),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("served_count").forGetter(Instance::servedCount),
                ItemPredicate.CODEC.optionalFieldOf("cost_item").forGetter(Instance::costItem),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("cost_count").forGetter(Instance::costCount),
                Codec.BOOL.optionalFieldOf("is_pet_item").forGetter(Instance::isPetItem),
                MinMaxBounds.Ints.CODEC
                        .optionalFieldOf("total_items_served")
                        .forGetter(Instance::totalItemsServed)
        ).apply(instance, Instance::new));

        public boolean matches(
                CustomerInternalEvents.ItemServed event,
                int currentTotalItemsServed
        ) {
            return spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && customerProfession.map(value -> value.equals(event.customerProfession())).orElse(true)
                    && servedItem.map(value -> value.test(event.servedItem())).orElse(true)
                    && servedCount.map(value -> value.matches(event.servedItem().getCount())).orElse(true)
                    && costItem.map(value -> value.test(event.costItem())).orElse(true)
                    && costCount.map(value -> value.matches(event.costItem().getCount())).orElse(true)
                    && isPetItem.map(value -> value == event.isPetItem()).orElse(true)
                    && totalItemsServed.map(value -> value.matches(currentTotalItemsServed)).orElse(true);
        }
    }
}
