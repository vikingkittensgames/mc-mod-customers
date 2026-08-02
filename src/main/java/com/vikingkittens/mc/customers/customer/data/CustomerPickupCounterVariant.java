package com.vikingkittens.mc.customers.customer.data;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public record CustomerPickupCounterVariant(
        String name,
        Supplier<? extends ItemLike> ingredient,
        Supplier<? extends Block> textureBlock,
        ResourceLocation sideTexture
) {
    public CustomerPickupCounterVariant {
        Objects.requireNonNull(name);
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(textureBlock);
        Objects.requireNonNull(sideTexture);
    }
}
