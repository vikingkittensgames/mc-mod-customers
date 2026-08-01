package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.Collection;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides version-independent writes for persisted customer and supplier data.
 */
public interface DataWriter {
    void putString(String key, String value);

    void putBoolean(String key, boolean value);

    void putBlockPos(String key, BlockPos value);

    void putBlockState(String key, BlockState value);

    void putUuid(String key, UUID value);

    void putUuids(String key, Collection<UUID> values);

    DataWriter child(String key);

    DataWriter addChild(String key);
}
