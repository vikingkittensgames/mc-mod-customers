package com.vikingkittens.mc.customers.customer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariants;

public final class CustomerPickupCounter {
    private static final IRegistrationHelper REGISTRATIONS = CustomersServices.registration();

    public static final Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPickupCounterBlock>>
            BLOCKS = registerBlocks();
    public static final Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Item, BlockItem>>
            ITEMS = registerItems();
    public static final CustomersRegistryEntry<BlockEntityType<?>, BlockEntityType<CustomerPickupCounterBlockEntity>>
            BLOCK_ENTITY = REGISTRATIONS.registerBlockEntityType(
            CustomerPickupCounterBlockEntity.NAME,
            CustomerPickupCounter::createBlockEntity,
            () -> BLOCKS.values().stream().map(CustomersRegistryEntry::get).toArray(CustomerPickupCounterBlock[]::new)
    );

    private CustomerPickupCounter() {
    }

    public static void initialize() {}

    public static String getBlockName(CustomerOverlayBlockVariant variant) {
        return variant.name() + "_customer_pickup_counter";
    }

    private static Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPickupCounterBlock>>
            registerBlocks() {
        Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPickupCounterBlock>> blocks =
                new LinkedHashMap<>();
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            blocks.put(
                    variant,
                    REGISTRATIONS.register(
                            Registries.BLOCK,
                            getBlockName(variant),
                            () -> new CustomerPickupCounterBlock(
                                    createProperties(variant)
                            )
                    )
            );
        }
        return Map.copyOf(blocks);
    }

    private static BlockBehaviour.Properties createProperties(CustomerOverlayBlockVariant variant) {
        Block source = variant.textureBlock().get();
        if (source instanceof RotatedPillarBlock) {
            source = Blocks.OAK_PLANKS;
        }
        return BlockBehaviour.Properties.copy(source)
                .noOcclusion();
    }

    private static Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Item, BlockItem>> registerItems() {
        Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Item, BlockItem>> items = new LinkedHashMap<>();
        for (Map.Entry<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPickupCounterBlock>> entry :
                BLOCKS.entrySet()) {
            items.put(
                    entry.getKey(),
                    REGISTRATIONS.register(
                            Registries.ITEM,
                            getBlockName(entry.getKey()),
                            () -> new BlockItem(entry.getValue().get(), new Item.Properties())
                    )
            );
        }
        return Map.copyOf(items);
    }

    private static CustomerPickupCounterBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomerPickupCounterBlockEntity(BLOCK_ENTITY.get(), pos, state);
    }

}
