package com.vikingkittens.mc.customers.compatability;

import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface IRegistrationHelper {
    <E extends BlockEntity> BlockEntityType<E> createBlockEntityType(
            BiFunction<BlockPos, BlockState, E> factory,
            Block... validBlocks
    );

    <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec);
}
