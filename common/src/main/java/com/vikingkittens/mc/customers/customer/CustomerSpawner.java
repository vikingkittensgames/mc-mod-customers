package com.vikingkittens.mc.customers.customer;

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

/***
 * Customer Spawner main feature class that covers registering the pieces
 * of this feature as part of the mod.
 *
 * It also provides type references for the registered pieces like
 * blocks, items, entities, etc to be used by this feature or other
 * features.
 */
public class CustomerSpawner {
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
    public static final RegistrySupplier<CustomerSpawnerBlock> CUSTOMER_SPAWNER_BLOCK = BLOCKS.register(
                    CustomerSpawnerBlock.NAME,
                    () -> new CustomerSpawnerBlock(
                            BlockBehaviour.Properties.of()
                    )
            );

    // -------------------- Block Entities --------------------
    public static final RegistrySupplier<BlockEntityType<CustomerSpawnerBlockEntity>> CUSTOMER_SPAWNER_ENTITY =
            BLOCK_ENTITY_TYPES.register(
            CustomerSpawnerBlockEntity.NAME,
            () -> BlockEntityType.Builder.of(CustomerSpawnerBlockEntity::new, CUSTOMER_SPAWNER_BLOCK.get()).build(null)
    );

    // -------------------- Items --------------------
    public static final RegistrySupplier<BlockItem> CUSTOMER_SPAWNER_ITEM = ITEMS.register(
                    CustomerSpawnerBlock.NAME,
                    () -> new BlockItem(
                            CUSTOMER_SPAWNER_BLOCK.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistrySupplier<MenuType<CustomerSpawnerBlockMenu>> CUSTOMER_SPAWNER_MENU = MENUS.register(
            "customer_spawner",
            () -> new MenuType<>(CustomerSpawnerBlockMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
    );

}
