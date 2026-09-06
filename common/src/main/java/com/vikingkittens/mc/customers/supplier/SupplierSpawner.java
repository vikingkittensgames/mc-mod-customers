package com.vikingkittens.mc.customers.supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.vikingkittens.mc.customers.compatability.BlockCUtils;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;
import com.vikingkittens.mc.customers.compatability.ItemCUtils;

public class SupplierSpawner {
    private static final IRegistrationHelper REGISTRATIONS =
            CustomersServices.registration();

    // -------------------- Registries --------------------
    public static void initialize() {}

    // -------------------- Blocks --------------------
    public static final CustomersRegistryEntry<Block, SupplierSpawnerBlock>
            SUPPLIER_SPAWNER_BLOCK = REGISTRATIONS.register(
                    Registries.BLOCK,
                    SupplierSpawnerBlock.NAME,
                    () -> new SupplierSpawnerBlock(
                            BlockCUtils.setId(BlockBehaviour.Properties.of(), SupplierSpawnerBlock.NAME)
                    )
            );

    // -------------------- Block Entities --------------------
    public static final CustomersRegistryEntry<
            BlockEntityType<?>,
            BlockEntityType<SupplierSpawnerBlockEntity>
    > SUPPLIER_SPAWNER_ENTITY = REGISTRATIONS.registerBlockEntityType(
            SupplierSpawnerBlockEntity.NAME,
            SupplierSpawnerBlockEntity::new,
            () -> new Block[] {SUPPLIER_SPAWNER_BLOCK.get()}
    );

    public static final CustomersRegistryEntry<
            MenuType<?>,
            MenuType<SupplierSpawnerBlockMenu>
    > SUPPLIER_SPAWNER_MENU = REGISTRATIONS.registerMenuType(
            "supplier_spawner",
            SupplierSpawnerBlockMenu::new
    );

    // -------------------- Items --------------------
    public static final CustomersRegistryEntry<Item, BlockItem>
            SUPPLIER_SPAWNER_ITEM = REGISTRATIONS.register(
                    Registries.ITEM,
                    SupplierSpawnerBlock.NAME,
                    () -> new BlockItem(
                            SUPPLIER_SPAWNER_BLOCK.get(),
                            ItemCUtils.setId(new Item.Properties(), SupplierSpawnerBlock.NAME)
                    )
            );

}
