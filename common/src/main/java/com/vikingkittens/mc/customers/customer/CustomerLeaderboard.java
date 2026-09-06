package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.compatability.BlockCUtils;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;
import com.vikingkittens.mc.customers.compatability.ItemCUtils;

public final class CustomerLeaderboard {
    private static final IRegistrationHelper REGISTRATIONS = CustomersServices.registration();

    public static final CustomersRegistryEntry<Block, CustomerLeaderboardBlock> BLOCK = REGISTRATIONS.register(
            Registries.BLOCK,
            CustomerLeaderboardBlock.NAME,
            () -> new CustomerLeaderboardBlock(
                    BlockCUtils.setId(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion(),
                            CustomerLeaderboardBlock.NAME
                    )
            )
    );
    public static final CustomersRegistryEntry<Item, BlockItem> ITEM = REGISTRATIONS.register(
            Registries.ITEM,
            CustomerLeaderboardBlock.NAME,
            () -> new BlockItem(BLOCK.get(), ItemCUtils.setId(new Item.Properties(), CustomerLeaderboardBlock.NAME))
    );
    public static final CustomersRegistryEntry<
            BlockEntityType<?>,
            BlockEntityType<CustomerLeaderboardBlockEntity>
    > BLOCK_ENTITY = REGISTRATIONS.registerBlockEntityType(
            CustomerLeaderboardBlockEntity.NAME,
            CustomerLeaderboard::createBlockEntity,
            () -> new Block[] {BLOCK.get()}
    );

    private CustomerLeaderboard() {}

    public static void initialize() {}

    private static CustomerLeaderboardBlockEntity createBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new CustomerLeaderboardBlockEntity(BLOCK_ENTITY.get(), pos, state);
    }
}
