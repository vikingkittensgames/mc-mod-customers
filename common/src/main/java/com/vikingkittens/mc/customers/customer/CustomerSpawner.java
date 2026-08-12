package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;

/***
 * Customer Spawner main feature class that covers registering the pieces
 * of this feature as part of the mod.
 *
 * It also provides type references for the registered pieces like
 * blocks, items, entities, etc to be used by this feature or other
 * features.
 */
public class CustomerSpawner {
    private static final IRegistrationHelper REGISTRATIONS =
            CustomersServices.registration();

    // -------------------- Registries --------------------
    public static void initialize() {}

    // -------------------- Blocks --------------------
    public static final CustomersRegistryEntry<Block, CustomerSpawnerBlock>
            CUSTOMER_SPAWNER_BLOCK = REGISTRATIONS.register(
                    Registries.BLOCK,
                    CustomerSpawnerBlock.NAME,
                    () -> new CustomerSpawnerBlock(
                            BlockBehaviour.Properties.of()
                    )
            );

    // -------------------- Block Entities --------------------
    public static final CustomersRegistryEntry<
            BlockEntityType<?>,
            BlockEntityType<CustomerSpawnerBlockEntity>
    > CUSTOMER_SPAWNER_ENTITY = REGISTRATIONS.registerBlockEntityType(
            CustomerSpawnerBlockEntity.NAME,
            CustomerSpawnerBlockEntity::new,
            () -> new Block[] {CUSTOMER_SPAWNER_BLOCK.get()}
    );

    // -------------------- Items --------------------
    public static final CustomersRegistryEntry<Item, BlockItem>
            CUSTOMER_SPAWNER_ITEM = REGISTRATIONS.register(
                    Registries.ITEM,
                    CustomerSpawnerBlock.NAME,
                    () -> new BlockItem(
                            CUSTOMER_SPAWNER_BLOCK.get(),
                            new Item.Properties()
                    )
            );
    public static final CustomersRegistryEntry<
            MenuType<?>,
            MenuType<CustomerSpawnerBlockMenu>
    > CUSTOMER_SPAWNER_MENU = REGISTRATIONS.registerMenuType(
            "customer_spawner",
            CustomerSpawnerBlockMenu::new
    );

}
