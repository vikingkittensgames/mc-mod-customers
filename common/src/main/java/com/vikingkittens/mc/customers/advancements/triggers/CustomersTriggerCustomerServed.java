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

public final class CustomersTriggerCustomerServed
        extends SimpleCriterionTrigger<CustomersTriggerCustomerServed.Instance> {
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
                MinMaxBounds.Ints.CODEC.optionalFieldOf("total_customers_served").forGetter(Instance::totalCustomersServed),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("total_casual_customers_served").forGetter(Instance::totalCustomersCasualServed),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("total_normal_customers_served").forGetter(Instance::totalCustomersNormalServed),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("total_impatient_customers_served").forGetter(Instance::totalCustomersImpatientServed)
        ).apply(instance, Instance::new));

        public boolean matches(
                CustomerInternalEvents.CustomerServed event,
                int currentTotalCustomersServed,
                int currentTotalCustomersCasualServed,
                int currentTotalCustomersNormalServed,
                int currentTotalCustomersImpatientServed
        ) {
            return spawnerMode.map(value -> value == event.spawnerMode()).orElse(true)
                    && customerProfession.map(value -> value.equals(event.customerProfession())).orElse(true)
                    && servedItem.map(value -> value.test(event.servedItem())).orElse(true)
                    && servedCount.map(value -> value.matches(event.servedItem().getCount())).orElse(true)
                    && costItem.map(value -> value.test(event.costItem())).orElse(true)
                    && costCount.map(value -> value.matches(event.costItem().getCount())).orElse(true)
                    && isPetItem.map(value -> value == event.isPetItem()).orElse(true)
                    && totalCustomersServed.map(value -> value.matches(currentTotalCustomersServed)).orElse(true)
                    && totalCustomersCasualServed.map(value -> value.matches(currentTotalCustomersCasualServed)).orElse(true)
                    && totalCustomersNormalServed.map(value -> value.matches(currentTotalCustomersNormalServed)).orElse(true)
                    && totalCustomersImpatientServed.map(value -> value.matches(currentTotalCustomersImpatientServed)).orElse(true);
        }
    }
}
