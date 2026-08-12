package com.vikingkittens.mc.customers.compatability;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface IRegistrationHelper {
    <T> CustomersRegistry<T> createRegistry(ResourceKey<Registry<T>> registryKey);

    <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec);

    <T, I extends T> CustomersRegistryEntry<T, I> register(
            ResourceKey<? extends Registry<T>> registryKey,
            String name,
            Supplier<? extends I> value
    );

    <E extends BlockEntity> CustomersRegistryEntry<BlockEntityType<?>, BlockEntityType<E>> registerBlockEntityType(
            String name,
            BiFunction<BlockPos, BlockState, E> factory,
            Supplier<? extends Block[]> validBlocks
    );

    <M extends AbstractContainerMenu> CustomersRegistryEntry<MenuType<?>, MenuType<M>> registerMenuType(
            String name,
            BiFunction<Integer, Inventory, M> factory
    );
}
