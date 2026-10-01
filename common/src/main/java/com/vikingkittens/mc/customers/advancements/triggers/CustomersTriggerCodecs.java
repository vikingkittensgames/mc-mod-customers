package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;

final class CustomersTriggerCodecs {
    static final Codec<ContextAwarePredicate> CONTEXT_AWARE_PREDICATE =
            jsonCodec(value -> ContextAwarePredicate.ANY, value -> JsonNull.INSTANCE);
    static final Codec<ItemPredicate> ITEM_PREDICATE =
            jsonCodec(ItemPredicate::fromJson, ItemPredicate::serializeToJson);
    static final Codec<MinMaxBounds.Ints> INT_RANGE =
            jsonCodec(MinMaxBounds.Ints::fromJson, MinMaxBounds.Ints::serializeToJson);
    static final Codec<MinMaxBounds.Doubles> DOUBLE_RANGE =
            jsonCodec(MinMaxBounds.Doubles::fromJson, MinMaxBounds.Doubles::serializeToJson);

    private CustomersTriggerCodecs() {
    }

    private static <T> Codec<T> jsonCodec(
            Function<JsonElement, T> decoder,
            Function<T, JsonElement> encoder
    ) {
        return Codec.PASSTHROUGH.xmap(
                dynamic -> decoder.apply(dynamic.convert(JsonOps.INSTANCE).getValue()),
                value -> new Dynamic<>(JsonOps.INSTANCE, encoder.apply(value))
        );
    }
}
