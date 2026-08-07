package com.vikingkittens.mc.customers.customer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.data.CustomerPickupCounterVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerPickupCounterVariants;

public final class CustomerPickupCounter {
    private static final DeferredRegister.Blocks BLOCK_REGISTER =
            DeferredRegister.createBlocks(Customers.MODID);
    private static final DeferredRegister.Items ITEM_REGISTER =
            DeferredRegister.createItems(Customers.MODID);
    private static final DeferredRegister<BlockEntityType<?>>
            BLOCK_ENTITY_REGISTER = DeferredRegister.create(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Customers.MODID
            );

    public static final Map<
            CustomerPickupCounterVariant,
            DeferredBlock<CustomerPickupCounterBlock>
    > BLOCKS = registerBlocks();
    public static final Map<
            CustomerPickupCounterVariant,
            DeferredItem<BlockItem>
    > ITEMS = registerItems();
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<CustomerPickupCounterBlockEntity>
    > BLOCK_ENTITY = BLOCK_ENTITY_REGISTER.register(
            CustomerPickupCounterBlockEntity.NAME,
            () -> new BlockEntityType<>(
                    CustomerPickupCounter::createBlockEntity,
                    BLOCKS.values().stream()
                            .map(DeferredBlock::get)
                            .toArray(CustomerPickupCounterBlock[]::new)
            )
    );

    private CustomerPickupCounter() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        ITEM_REGISTER.register(modEventBus);
        BLOCK_ENTITY_REGISTER.register(modEventBus);
        modEventBus.addListener(CustomerPickupCounter::addCreative);
    }

    public static String getBlockName(
            CustomerPickupCounterVariant variant
    ) {
        return variant.name() + "_customer_pickup_counter";
    }

    private static Map<
            CustomerPickupCounterVariant,
            DeferredBlock<CustomerPickupCounterBlock>
    > registerBlocks() {
        Map<
                CustomerPickupCounterVariant,
                DeferredBlock<CustomerPickupCounterBlock>
        > blocks = new LinkedHashMap<>();
        for (CustomerPickupCounterVariant variant
                : CustomerPickupCounterVariants.ALL) {
            blocks.put(
                    variant,
                    BLOCK_REGISTER.registerBlock(
                            getBlockName(variant),
                            CustomerPickupCounterBlock::new,
                            () -> createProperties(variant)
                    )
            );
        }
        return Map.copyOf(blocks);
    }

    private static BlockBehaviour.Properties createProperties(
            CustomerPickupCounterVariant variant
    ) {
        Block source = variant.textureBlock().get();
        if (source instanceof RotatedPillarBlock) {
            source = Blocks.OAK_PLANKS;
        }
        return BlockBehaviour.Properties.ofFullCopy(source)
                .noOcclusion();
    }

    private static Map<
            CustomerPickupCounterVariant,
            DeferredItem<BlockItem>
    > registerItems() {
        Map<CustomerPickupCounterVariant, DeferredItem<BlockItem>> items =
                new LinkedHashMap<>();
        for (Map.Entry<
                CustomerPickupCounterVariant,
                DeferredBlock<CustomerPickupCounterBlock>
        > entry : BLOCKS.entrySet()) {
            items.put(
                    entry.getKey(),
                    ITEM_REGISTER.registerSimpleBlockItem(
                            getBlockName(entry.getKey()),
                            entry.getValue()
                    )
            );
        }
        return Map.copyOf(items);
    }

    private static CustomerPickupCounterBlockEntity createBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new CustomerPickupCounterBlockEntity(
                BLOCK_ENTITY.get(),
                pos,
                state
        );
    }

    private static void addCreative(
            BuildCreativeModeTabContentsEvent event
    ) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ITEMS.values().forEach(event::accept);
        }
    }
}
