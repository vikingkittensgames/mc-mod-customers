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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariants;

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
            CustomerOverlayBlockVariant,
            DeferredBlock<CustomerPickupCounterBlock>
    > BLOCKS = registerBlocks();
    public static final Map<
            CustomerOverlayBlockVariant,
            DeferredItem<BlockItem>
    > ITEMS = registerItems();
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<CustomerPickupCounterBlockEntity>
    > BLOCK_ENTITY = BLOCK_ENTITY_REGISTER.register(
            CustomerPickupCounterBlockEntity.NAME,
            () -> BlockEntityType.Builder.of(
                    CustomerPickupCounter::createBlockEntity,
                    BLOCKS.values().stream()
                            .map(DeferredBlock::get)
                            .toArray(CustomerPickupCounterBlock[]::new)
            ).build(null)
    );

    private CustomerPickupCounter() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        ITEM_REGISTER.register(modEventBus);
        BLOCK_ENTITY_REGISTER.register(modEventBus);
        modEventBus.addListener(CustomerPickupCounter::addCreative);
        modEventBus.addListener(
                CustomerPickupCounter::registerCapabilities
        );
    }

    public static String getBlockName(
            CustomerOverlayBlockVariant variant
    ) {
        return variant.name() + "_customer_pickup_counter";
    }

    private static Map<
            CustomerOverlayBlockVariant,
            DeferredBlock<CustomerPickupCounterBlock>
    > registerBlocks() {
        Map<
                CustomerOverlayBlockVariant,
                DeferredBlock<CustomerPickupCounterBlock>
        > blocks = new LinkedHashMap<>();
        for (CustomerOverlayBlockVariant variant
                : CustomerOverlayBlockVariants.ALL) {
            blocks.put(
                    variant,
                    BLOCK_REGISTER.register(
                            getBlockName(variant),
                            () -> new CustomerPickupCounterBlock(
                                    createProperties(variant)
                            )
                    )
            );
        }
        return Map.copyOf(blocks);
    }

    private static BlockBehaviour.Properties createProperties(
            CustomerOverlayBlockVariant variant
    ) {
        Block source = variant.textureBlock().get();
        if (source instanceof RotatedPillarBlock) {
            source = Blocks.OAK_PLANKS;
        }
        return BlockBehaviour.Properties.ofFullCopy(source)
                .noOcclusion();
    }
    private static Map<
            CustomerOverlayBlockVariant,
            DeferredItem<BlockItem>
    > registerItems() {
        Map<CustomerOverlayBlockVariant, DeferredItem<BlockItem>> items =
                new LinkedHashMap<>();
        for (Map.Entry<
                CustomerOverlayBlockVariant,
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

    private static void registerCapabilities(
            RegisterCapabilitiesEvent event
    ) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (counter, direction) -> counter.getItemHandler()
        );
    }
}
