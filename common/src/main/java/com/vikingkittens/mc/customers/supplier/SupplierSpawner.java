package com.vikingkittens.mc.customers.supplier;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.vikingkittens.mc.customers.Customers;

public class SupplierSpawner {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Customers.MODID, Registries.BLOCK);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Customers.MODID, Registries.ITEM);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Customers.MODID, Registries.MENU);

    // -------------------- Registries --------------------
    public static void initialize() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
        ITEMS.register();
        MENUS.register();
    }

    // -------------------- Blocks --------------------
    public static final RegistrySupplier<SupplierSpawnerBlock> SUPPLIER_SPAWNER_BLOCK = BLOCKS.register(
                    SupplierSpawnerBlock.NAME,
                    () -> new SupplierSpawnerBlock(
                            BlockBehaviour.Properties.of()
                    )
            );

    // -------------------- Block Entities --------------------
    public static final RegistrySupplier<BlockEntityType<SupplierSpawnerBlockEntity>> SUPPLIER_SPAWNER_ENTITY =
            BLOCK_ENTITY_TYPES.register(
            SupplierSpawnerBlockEntity.NAME,
            () -> BlockEntityType.Builder.of(SupplierSpawnerBlockEntity::new, SUPPLIER_SPAWNER_BLOCK.get()).build(null)
    );

    public static final RegistrySupplier<MenuType<SupplierSpawnerBlockMenu>> SUPPLIER_SPAWNER_MENU = MENUS.register(
            "supplier_spawner",
            () -> new MenuType<>(SupplierSpawnerBlockMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
    );

    // -------------------- Items --------------------
    public static final RegistrySupplier<BlockItem> SUPPLIER_SPAWNER_ITEM = ITEMS.register(
                    SupplierSpawnerBlock.NAME,
                    () -> new BlockItem(
                            SUPPLIER_SPAWNER_BLOCK.get(),
                            new Item.Properties()
                    )
            );

}
