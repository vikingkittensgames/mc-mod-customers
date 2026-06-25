package com.vikingkittens.mc.customers.spawner;

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

/***
 * Customer Spawner main feature class that covers registering the pieces
 * of this feature as part of the mod.
 *
 * It also provides type references for the registered pieces like
 * blocks, items, entities, etc to be used by this feature or other
 * features.
 */
public class CustomerSpawner {
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

        modEventBus.addListener(CustomerSpawner::addCreative);
    }

    // -------------------- Blocks --------------------
    public static final DeferredBlock<Block> CUSTOMER_SPAWNER_BLOCK = blocks.registerBlock(CustomerSpawnerBlock.NAME, CustomerSpawnerBlock::new);

    // -------------------- Block Entities --------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomerSpawnerBlockEntity>> CUSTOMER_SPAWNER_ENTITY = blockEntities.register(CustomerSpawnerBlockEntity.NAME, () -> BlockEntityType.Builder.of(CustomerSpawnerBlockEntity::new, CUSTOMER_SPAWNER_BLOCK.get()).build(null));

    // -------------------- Items --------------------
    public static final DeferredItem<BlockItem> CUSTOMER_SPAWNER_ITEM = items.registerSimpleBlockItem(CustomerSpawnerBlock.NAME, CUSTOMER_SPAWNER_BLOCK);

    // Add each item to the right creative tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(CUSTOMER_SPAWNER_ITEM);
        }
    }
}
