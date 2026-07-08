package com.vikingkittens.mc.customers.supplier;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

public class SupplierSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String modid = Customers.MODID;

    // -------------------- Registries --------------------
    private static final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(modid);
    private static final DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, modid);
    private static final DeferredRegister.Items items = DeferredRegister.createItems(Customers.MODID);

    // -------------------- Registries --------------------
    public static void register(IEventBus modEventBus) {
        LOGGER.info("Registering components");

        blocks.register(modEventBus);
        blockEntities.register(modEventBus);
        items.register(modEventBus);

        modEventBus.addListener(SupplierSpawner::addCreative);
    }

    // -------------------- Blocks --------------------
    public static final DeferredBlock<Block> SUPPLIER_SPAWNER_BLOCK = blocks.registerBlock(SupplierSpawnerBlock.NAME, SupplierSpawnerBlock::new);

    // -------------------- Block Entities --------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SupplierSpawnerBlockEntity>> SUPPLIER_SPAWNER_ENTITY = blockEntities.register(SupplierSpawnerBlockEntity.NAME, () -> BlockEntityType.Builder.of(SupplierSpawnerBlockEntity::new, SUPPLIER_SPAWNER_BLOCK.get()).build(null));

    // -------------------- Items --------------------
    public static final DeferredItem<BlockItem> SUPPLIER_SPAWNER_ITEM = items.registerSimpleBlockItem(SupplierSpawnerBlock.NAME, SUPPLIER_SPAWNER_BLOCK);

    // Add each item to the right creative tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SUPPLIER_SPAWNER_ITEM);
        }
    }
}
