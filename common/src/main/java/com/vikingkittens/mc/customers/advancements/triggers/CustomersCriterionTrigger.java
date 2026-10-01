package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.function.Predicate;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;

public abstract class CustomersCriterionTrigger<T>
        extends SimpleCriterionTrigger<CustomersCriterionTriggerInstance<T>> {
    private final ResourceLocation id;
    private final CustomersTriggerSchema<T> schema;

    protected CustomersCriterionTrigger(String name, CustomersTriggerSchema<T> schema) {
        id = ResourceLocationCUtils.create(Customers.MODID, name);
        this.schema = schema;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected CustomersCriterionTriggerInstance<T> createInstance(
            JsonObject json,
            ContextAwarePredicate player,
            DeserializationContext context
    ) {
        return new CustomersCriterionTriggerInstance<>(id, schema, schema.decodeAdvancement(json, player));
    }

    protected final void triggerValue(ServerPlayer player, Predicate<T> predicate) {
        trigger(player, instance -> predicate.test(instance.value()));
    }
}
