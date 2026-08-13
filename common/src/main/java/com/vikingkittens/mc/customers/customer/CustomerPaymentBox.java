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

public final class CustomerPaymentBox {
    private static final IRegistrationHelper REGISTRATIONS = CustomersServices.registration();

    public static final Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPaymentBoxBlock>>
            BLOCKS = registerBlocks();
    public static final Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Item, BlockItem>>
            ITEMS = registerItems();
    public static final CustomersRegistryEntry<BlockEntityType<?>, BlockEntityType<CustomerPaymentBoxBlockEntity>>
            BLOCK_ENTITY = REGISTRATIONS.registerBlockEntityType(
            CustomerPaymentBoxBlockEntity.NAME,
            CustomerPaymentBox::createBlockEntity,
            () -> BLOCKS.values().stream().map(CustomersRegistryEntry::get).toArray(CustomerPaymentBoxBlock[]::new)
    );

    private CustomerPaymentBox() {
    }

    public static void initialize() {}

    public static String getBlockName(CustomerOverlayBlockVariant variant) {
        return variant.name() + "_customer_payment_box";
    }

    private static Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPaymentBoxBlock>>
            registerBlocks() {
        Map<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPaymentBoxBlock>> blocks =
                new LinkedHashMap<>();
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            blocks.put(
                    variant,
                    REGISTRATIONS.register(
                            Registries.BLOCK,
                            getBlockName(variant),
                            () -> new CustomerPaymentBoxBlock(
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
        for (Map.Entry<CustomerOverlayBlockVariant, CustomersRegistryEntry<Block, CustomerPaymentBoxBlock>> entry :
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

    private static CustomerPaymentBoxBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomerPaymentBoxBlockEntity(BLOCK_ENTITY.get(), pos, state);
    }

}
