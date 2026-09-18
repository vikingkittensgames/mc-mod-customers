package com.vikingkittens.mc.customers.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;

public record CustomersLocationPredicate(ResourceKey<Level> dimension, BlockPos position) {
    public static final Codec<CustomersLocationPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CustomersLocationPredicate::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(CustomersLocationPredicate::position)
    ).apply(instance, CustomersLocationPredicate::new));

    public boolean matches(CustomerInternalEvents.CustomerEvent event) {
        return matches(event.level(), event.spawnerPosition());
    }

    public boolean matches(Level level, BlockPos position) {
        return dimension.equals(level.dimension()) && this.position.equals(position);
    }
}
