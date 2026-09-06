package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.vikingkittens.mc.customers.Customers;

public final class BlockCUtils {
    private BlockCUtils() {}

    public static BlockBehaviour.Properties setId(BlockBehaviour.Properties properties, String name) {
        return properties.setId(ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Customers.MODID, name)
        ));
    }
}
