package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Adapts Minecraft 1.21.11 value output to the shared persistence writer.
 */
final class ValueOutputDataWriter implements DataWriter {
    private final ValueOutput output;
    private final Map<String, ValueOutput.ValueOutputList> childLists = new HashMap<>();

    ValueOutputDataWriter(ValueOutput output) {
        this.output = output;
    }

    @Override
    public void putString(String key, String value) {
        output.putString(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        output.putBoolean(key, value);
    }

    @Override
    public void putBlockPos(String key, BlockPos value) {
        output.store(key, BlockPos.CODEC, value);
    }

    @Override
    public void putBlockState(String key, BlockState value) {
        output.store(key, BlockState.CODEC, value);
    }

    @Override
    public void putUuid(String key, UUID value) {
        output.store(key, UUIDUtil.CODEC, value);
    }

    @Override
    public void putUuids(String key, Collection<UUID> values) {
        ValueOutput.TypedOutputList<UUID> list = output.list(key, UUIDUtil.CODEC);
        values.forEach(list::add);
    }

    @Override
    public void putItemStacks(String key, List<ItemStack> values) {
        ValueOutput.TypedOutputList<ItemStack> list = output.list(key, ItemStack.CODEC);
        values.forEach(list::add);
    }

    @Override
    public DataWriter child(String key) {
        return new ValueOutputDataWriter(output.child(key));
    }

    @Override
    public DataWriter addChild(String key) {
        return new ValueOutputDataWriter(
                childLists.computeIfAbsent(key, output::childrenList).addChild()
        );
    }
}
