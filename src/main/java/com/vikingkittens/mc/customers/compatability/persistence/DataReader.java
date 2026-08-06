package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides version-independent access to persisted customer and supplier data.
 */
public interface DataReader {
    Optional<String> getString(String key);

    boolean getBoolean(String key);

    Optional<BlockPos> getBlockPos(String key);

    Optional<BlockState> getBlockState(String key);

    Optional<UUID> getUuid(String key);

    List<UUID> getUuids(String key);

    List<ItemStack> getItemStacks(String key);

    DataReader childOrEmpty(String key);

    List<DataReader> getChildren(String key);
}
