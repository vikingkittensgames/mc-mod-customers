package com.vikingkittens.mc.customers.compatability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.Customers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class VanillaRegistrationHelper implements IRegistrationHelper {
    private final Map<ResourceKey<?>, MappedRegistry<?>> registries = new HashMap<>();

    @Override
    public <T> CustomersRegistry<T> createRegistry(ResourceKey<Registry<T>> registryKey) {
        MappedRegistry<T> registry = createMappedRegistry(registryKey);
        registries.put(registryKey, registry);
        return new CustomersRegistry<>(registryKey, registry::freeze);
    }

    @Override
    public <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {}

    @Override
    public <T, I extends T> CustomersRegistryEntry<T, I> register(
            ResourceKey<? extends Registry<T>> registryKey,
            String name,
            Supplier<? extends I> valueFactory
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(Customers.MODID, name);
        ResourceKey<T> key = ResourceKey.create(registryKey, id);
        if (!registries.containsKey(registryKey)) {
            return new CustomersRegistryEntry<>(key, valueFactory);
        }
        MappedRegistry<T> registry = findRegistry(registryKey);
        I value = valueFactory.get();
        registry.register(key, value, RegistrationInfo.BUILT_IN);
        return new CustomersRegistryEntry<>(key, () -> value);
    }

    @Override
    public <E extends BlockEntity> CustomersRegistryEntry<BlockEntityType<?>, BlockEntityType<E>> registerBlockEntityType(
            String name,
            BiFunction<BlockPos, BlockState, E> factory,
            Supplier<? extends Block[]> validBlocks
    ) {
        @SuppressWarnings("unchecked")
        BlockEntityType<E> type = mock(BlockEntityType.class);
        when(type.create(any(BlockPos.class), any(BlockState.class)))
                .thenAnswer(invocation -> factory.apply(invocation.getArgument(0), invocation.getArgument(1)));
        when(type.isValid(any(BlockState.class)))
                .thenAnswer(invocation -> Set.of(validBlocks.get()).contains(((BlockState)invocation.getArgument(0)).getBlock()));
        return register(
                Registries.BLOCK_ENTITY_TYPE,
                name,
                () -> type
        );
    }

    @Override
    public <M extends AbstractContainerMenu> CustomersRegistryEntry<MenuType<?>, MenuType<M>> registerMenuType(
            String name,
            BiFunction<Integer, Inventory, M> factory
    ) {
        @SuppressWarnings("unchecked")
        MenuType<M> type = mock(MenuType.class);
        return register(Registries.MENU, name, () -> type);
    }

    @SuppressWarnings("unchecked")
    private <T> MappedRegistry<T> findRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        return (MappedRegistry<T>)registries.get(registryKey);
    }

    private static <T> MappedRegistry<T> createMappedRegistry(ResourceKey<Registry<T>> registryKey) {
        return new MappedRegistry<>(registryKey, Lifecycle.stable(), false);
    }
}
