package com.vikingkittens.mc.customers.customer.data;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public record CustomerPickupCounterVariant(
        String name,
        Supplier<? extends ItemLike> ingredient,
        Supplier<? extends Block> textureBlock,
        Identifier sideTexture
) {
    public CustomerPickupCounterVariant {
        Objects.requireNonNull(name);
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(textureBlock);
        Objects.requireNonNull(sideTexture);
    }
}
