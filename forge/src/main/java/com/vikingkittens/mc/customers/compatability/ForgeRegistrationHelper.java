package com.vikingkittens.mc.customers.compatability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import com.vikingkittens.mc.customers.Customers;

public final class ForgeRegistrationHelper implements IRegistrationHelper {
    private final List<DeferredRegister<?>> registers = new ArrayList<>();
    private final List<Consumer<DataPackRegistryEvent.NewRegistry>> dataPackRegistries = new ArrayList<>();
    private IEventBus eventBus;

    @Override
    public synchronized <T> CustomersRegistry<T> createRegistry(ResourceKey<Registry<T>> registryKey) {
        DeferredRegister<T> register = DeferredRegister.create(registryKey, Customers.MODID);
        register.makeRegistry(() -> RegistryBuilder.<T>of().hasTags());
        registers.add(register);
        if (eventBus != null) {
            register.register(eventBus);
        }
        return new CustomersRegistry<>(registryKey, () -> findRegistry(registryKey));
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
        RegistryObject<I> holder = register.register(name, value);
        registers.add(register);
        if (eventBus != null) {
            register.register(eventBus);
        }
        ResourceKey<T> key = ResourceKey.create(
                registryKey,
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, name)
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
                () -> BlockEntityType.Builder.of(factory::apply, validBlocks.get()).build(null)
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
            throw new IllegalStateException("Forge registration is already bound");
        }
        this.eventBus = Objects.requireNonNull(eventBus);
        eventBus.addListener(this::registerDataPackRegistries);
        registers.forEach(register -> register.register(eventBus));
    }

    void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        dataPackRegistries.forEach(registration -> registration.accept(event));
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> findRegistry(ResourceKey<Registry<T>> registryKey) {
        return (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    }
}
