package com.vikingkittens.mc.customers.compatability;

import java.util.Set;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class VanillaRegistrationHelper implements IRegistrationHelper {
    @Override
    public <E extends BlockEntity> BlockEntityType<E> createBlockEntityType(
            BiFunction<BlockPos, BlockState, E> factory,
            Block... validBlocks
    ) {
        @SuppressWarnings("unchecked")
        BlockEntityType<E> type = mock(BlockEntityType.class);
        when(type.create(any(BlockPos.class), any(BlockState.class)))
                .thenAnswer(invocation -> factory.apply(invocation.getArgument(0), invocation.getArgument(1)));
        when(type.isValid(any(BlockState.class)))
                .thenAnswer(invocation -> Set.of(validBlocks)
                        .contains(invocation.<BlockState>getArgument(0).getBlock()));
        return type;
    }

    @Override
    public <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {}
}
