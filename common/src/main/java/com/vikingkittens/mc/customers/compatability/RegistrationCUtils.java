package com.vikingkittens.mc.customers.compatability;

import java.util.function.Function;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.Customers;

public final class RegistrationCUtils {
    private RegistrationCUtils() {}

    public static <B extends Block> RegistrySupplier<B> registerBlock(
            DeferredRegister<Block> registry,
            String name,
            Function<ResourceKey<Block>, B> factory
    ) {
        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Customers.MODID, name)
        );
        return registry.register(name, () -> factory.apply(key));
    }

    public static <I extends Item> RegistrySupplier<I> registerItem(
            DeferredRegister<Item> registry,
            String name,
            Function<ResourceKey<Item>, I> factory
    ) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(Customers.MODID, name)
        );
        return registry.register(name, () -> factory.apply(key));
    }
}
