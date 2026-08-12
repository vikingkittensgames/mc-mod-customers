package com.vikingkittens.mc.customers.compatability;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class CustomersRegistry<T> implements Supplier<Registry<T>> {
    private final ResourceKey<Registry<T>> key;
    private final Supplier<Registry<T>> registry;

    public CustomersRegistry(ResourceKey<Registry<T>> key, Supplier<Registry<T>> registry) {
        this.key = Objects.requireNonNull(key);
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public Registry<T> get() {
        return Objects.requireNonNull(registry.get(), () -> "Registry is not available yet: " + key.location());
    }

    public ResourceKey<Registry<T>> getKey() {
        return key;
    }
}
