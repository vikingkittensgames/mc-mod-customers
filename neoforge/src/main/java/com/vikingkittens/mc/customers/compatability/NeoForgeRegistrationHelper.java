package com.vikingkittens.mc.customers.compatability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import com.vikingkittens.mc.customers.Customers;

public final class NeoForgeRegistrationHelper implements IRegistrationHelper {
    private final List<DeferredRegister<?>> registers = new ArrayList<>();
    private final List<Registry<?>> customRegistries = new ArrayList<>();
    private final List<Consumer<DataPackRegistryEvent.NewRegistry>> dataPackRegistries = new ArrayList<>();
    private IEventBus eventBus;

    @Override
    public synchronized <T> CustomersRegistry<T> createRegistry(ResourceKey<Registry<T>> registryKey) {
        Registry<T> registry = new RegistryBuilder<>(registryKey).create();
        customRegistries.add(registry);
        return new CustomersRegistry<>(registryKey, () -> registry);
    }

    @Override
    public synchronized <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
        dataPackRegistries.add(event -> event.dataPackRegistry(registryKey, codec, codec));
    }

    @Override
    public synchronized <T, I extends T> CustomersRegistryEntry<T, I> register(
            ResourceKey<? extends Registry<T>> registryKey,
            String name,
            Supplier<? extends I> value
    ) {
        DeferredRegister<T> register =
                DeferredRegister.create(registryKey, Customers.MODID);
        DeferredHolder<T, I> holder = register.register(name, value);
        registers.add(register);
        if (eventBus != null) {
            register.register(eventBus);
        }
        ResourceKey<T> key = ResourceKey.create(
                registryKey,
                Identifier.fromNamespaceAndPath(Customers.MODID, name)
        );
        return new CustomersRegistryEntry<>(key, holder);
    }

    @Override
    public <E extends BlockEntity> CustomersRegistryEntry<BlockEntityType<?>, BlockEntityType<E>> registerBlockEntityType(
            String name,
            BiFunction<BlockPos, BlockState, E> factory,
            Supplier<? extends Block[]> validBlocks
    ) {
        return register(
                Registries.BLOCK_ENTITY_TYPE,
                name,
                () -> new BlockEntityType<>(factory::apply, Set.of(validBlocks.get()))
        );
    }

    @Override
    public <M extends AbstractContainerMenu> CustomersRegistryEntry<MenuType<?>, MenuType<M>> registerMenuType(
            String name,
            BiFunction<Integer, Inventory, M> factory
    ) {
        return register(
                Registries.MENU,
                name,
                () -> new MenuType<>(factory::apply, FeatureFlags.DEFAULT_FLAGS)
        );
    }

    public synchronized void bind(IEventBus eventBus) {
        if (this.eventBus != null) {
            throw new IllegalStateException("NeoForge registration is already bound");
        }
        this.eventBus = Objects.requireNonNull(eventBus);
        eventBus.addListener(this::registerCustomRegistries);
        eventBus.addListener(this::registerDataPackRegistries);
        registers.forEach(register -> register.register(eventBus));
    }

    void registerCustomRegistries(NewRegistryEvent event) {
        customRegistries.forEach(event::register);
    }

    void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        dataPackRegistries.forEach(registration -> registration.accept(event));
    }
}
