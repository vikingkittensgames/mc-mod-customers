package com.vikingkittens.mc.customers.advancements.triggers;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record CustomersLocationPredicate(ResourceKey<Level> dimension, BlockPos position) {
    public static final Codec<CustomersLocationPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CustomersLocationPredicate::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(CustomersLocationPredicate::position)
    ).apply(instance, CustomersLocationPredicate::new));

    public boolean matches(Level level, @Nullable BlockPos position) {
        return dimension.equals(level.dimension()) && this.position.equals(position);
    }
}
