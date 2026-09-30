package com.vikingkittens.mc.customers.customer;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.RegistrationCUtils;

public final class CustomerLeaderboard {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Customers.MODID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Customers.MODID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<CustomerLeaderboardBlock> BLOCK = RegistrationCUtils.registerBlock(
            BLOCKS,
            CustomerLeaderboardBlock.NAME,
            key -> new CustomerLeaderboardBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                            .noOcclusion()
                            .setId(key)
            )
    );
    public static final RegistrySupplier<BlockItem> ITEM = RegistrationCUtils.registerItem(
            ITEMS,
            CustomerLeaderboardBlock.NAME,
            key -> new BlockItem(
                    BLOCK.get(),
                    new Item.Properties()
                            .setId(key)
                            .useBlockDescriptionPrefix()
            )
    );
    public static final RegistrySupplier<BlockEntityType<CustomerLeaderboardBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
            CustomerLeaderboardBlockEntity.NAME,
            () -> CustomersServices.registration().createBlockEntityType(
                    CustomerLeaderboard::createBlockEntity,
                    BLOCK.get()
            )
    );

    private CustomerLeaderboard() {}

    public static void initialize() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
    }

    private static CustomerLeaderboardBlockEntity createBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new CustomerLeaderboardBlockEntity(BLOCK_ENTITY.get(), pos, state);
    }
}
