package com.vikingkittens.mc.customers.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public final class CustomersCriterionTriggerInstance<T> extends AbstractCriterionTriggerInstance {
    private final CustomersTriggerSchema<T> schema;
    private final T value;

    CustomersCriterionTriggerInstance(
            ResourceLocation criterion,
            CustomersTriggerSchema<T> schema,
            T value
    ) {
        super(criterion, schema.playerPredicate(value));
        this.schema = schema;
        this.value = value;
    }

    public T value() {
        return value;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext context) {
        return schema.serializeAdvancement(value, context);
    }
}
