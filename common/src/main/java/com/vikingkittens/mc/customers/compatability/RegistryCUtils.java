package com.vikingkittens.mc.customers.compatability;

import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class RegistryCUtils {
    private RegistryCUtils() {}

    public static Identifier getIdentifier(ResourceKey<?> key) {
        return key.identifier();
    }

    public static <T> Optional<Registry<T>> lookup(
            RegistryAccess registryAccess,
            ResourceKey<? extends Registry<? extends T>> key
    ) {
        return registryAccess.lookup(key);
    }

    public static <T> Registry<T> lookupOrThrow(
            RegistryAccess registryAccess,
            ResourceKey<? extends Registry<? extends T>> key
    ) {
        return registryAccess.lookupOrThrow(key);
    }

    public static <T> T getValue(
            Registry<T> registry,
            Identifier identifier
    ) {
        return registry.getValue(identifier);
    }
}
