package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.Customers;

public final class CustomerBlockTags {
    public static final TagKey<Block> CAN_NOT_AVOID = create("can_not_avoid");
    public static final TagKey<Block> CAN_AVOID = create("can_avoid");

    private CustomerBlockTags() {}

    private static TagKey<Block> create(String name) {
        return TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, name)
        );
    }
}
