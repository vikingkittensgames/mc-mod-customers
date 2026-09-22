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

public final class CustomerLeaderboard {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Customers.MODID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Customers.MODID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<CustomerLeaderboardBlock> BLOCK = BLOCKS.register(
            CustomerLeaderboardBlock.NAME,
            () -> new CustomerLeaderboardBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()
            )
    );
    public static final RegistrySupplier<BlockItem> ITEM = ITEMS.register(
            CustomerLeaderboardBlock.NAME,
            () -> new BlockItem(BLOCK.get(), new Item.Properties())
    );
    public static final RegistrySupplier<BlockEntityType<CustomerLeaderboardBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
            CustomerLeaderboardBlockEntity.NAME,
            () -> BlockEntityType.Builder.of(CustomerLeaderboard::createBlockEntity, BLOCK.get()).build(null)
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
