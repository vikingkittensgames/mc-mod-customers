package com.vikingkittens.mc.customers.compatability;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class CustomersRegistryEntry<T, I extends T> implements Supplier<I> {
    private final ResourceKey<T> key;
    private final Supplier<? extends I> value;

    public CustomersRegistryEntry(
            ResourceKey<T> key,
            Supplier<? extends I> value
    ) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public I get() {
        return value.get();
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    public Identifier getId() {
        return RegistryCUtils.getIdentifier(key);
    }
}
