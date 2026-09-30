package com.vikingkittens.mc.customers.customer;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

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

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.RegistrationCUtils;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariants;

public final class CustomerPaymentBox {
    private static final DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(Customers.MODID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(Customers.MODID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final Map<CustomerOverlayBlockVariant, RegistrySupplier<CustomerPaymentBoxBlock>>
            BLOCKS = registerBlocks();
    public static final Map<CustomerOverlayBlockVariant, RegistrySupplier<BlockItem>>
            ITEMS = registerItems();
    public static final RegistrySupplier<BlockEntityType<CustomerPaymentBoxBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
            CustomerPaymentBoxBlockEntity.NAME,
            () -> CustomersServices.registration().createBlockEntityType(
                    CustomerPaymentBox::createBlockEntity,
                    BLOCKS.values().stream()
                            .map(RegistrySupplier::get)
                            .toArray(CustomerPaymentBoxBlock[]::new)
            )
    );

    private CustomerPaymentBox() {
    }

    public static void initialize() {
        BLOCK_REGISTRY.register();
        ITEM_REGISTRY.register();
        BLOCK_ENTITY_TYPES.register();
    }

    public static String getBlockName(CustomerOverlayBlockVariant variant) {
        return variant.name() + "_customer_payment_box";
    }

    private static Map<CustomerOverlayBlockVariant, RegistrySupplier<CustomerPaymentBoxBlock>>
            registerBlocks() {
        Map<CustomerOverlayBlockVariant, RegistrySupplier<CustomerPaymentBoxBlock>> blocks =
                new LinkedHashMap<>();
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            blocks.put(
                    variant,
                    RegistrationCUtils.registerBlock(
                            BLOCK_REGISTRY,
                            getBlockName(variant),
                            key -> new CustomerPaymentBoxBlock(
                                    createProperties(variant).setId(key)
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
        return BlockBehaviour.Properties.ofFullCopy(source)
                .noOcclusion();
    }

    private static Map<CustomerOverlayBlockVariant, RegistrySupplier<BlockItem>> registerItems() {
        Map<CustomerOverlayBlockVariant, RegistrySupplier<BlockItem>> items = new LinkedHashMap<>();
        for (Map.Entry<CustomerOverlayBlockVariant, RegistrySupplier<CustomerPaymentBoxBlock>> entry :
                BLOCKS.entrySet()) {
            items.put(
                    entry.getKey(),
                    RegistrationCUtils.registerItem(
                            ITEM_REGISTRY,
                            getBlockName(entry.getKey()),
                            key -> new BlockItem(
                                    entry.getValue().get(),
                                    new Item.Properties()
                                            .setId(key)
                                            .useBlockDescriptionPrefix()
                            )
                    )
            );
        }
        return Map.copyOf(items);
    }

    private static CustomerPaymentBoxBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomerPaymentBoxBlockEntity(BLOCK_ENTITY.get(), pos, state);
    }

}
