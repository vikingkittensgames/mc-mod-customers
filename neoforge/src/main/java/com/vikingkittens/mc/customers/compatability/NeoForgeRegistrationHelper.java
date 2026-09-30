package com.vikingkittens.mc.customers.compatability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class NeoForgeRegistrationHelper implements IRegistrationHelper {
    private final List<Consumer<DataPackRegistryEvent.NewRegistry>> dataPackRegistries = new ArrayList<>();
    private IEventBus eventBus;

    @Override
    public <E extends BlockEntity> BlockEntityType<E> createBlockEntityType(
            BiFunction<BlockPos, BlockState, E> factory,
            Block... validBlocks
    ) {
        return new BlockEntityType<>(factory::apply, Set.of(validBlocks));
    }

    @Override
    public synchronized <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
        dataPackRegistries.add(event -> event.dataPackRegistry(registryKey, codec, codec));
    }

    public synchronized void bind(IEventBus eventBus) {
        if (this.eventBus != null) {
            throw new IllegalStateException("NeoForge registration is already bound");
        }
        this.eventBus = Objects.requireNonNull(eventBus);
        eventBus.addListener(this::registerDataPackRegistries);
    }

    void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        dataPackRegistries.forEach(registration -> registration.accept(event));
    }
}
