package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;

final class ValueOutputDataWriter implements DataWriter {
    private final ValueOutput output;

    ValueOutputDataWriter(ValueOutput output) {
        this.output = output;
    }

    @Override
    public void putString(String key, String value) {
        output.putString(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        output.putFloat(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        output.putInt(key, value);
    }

    @Override
    public void putStrings(String key, Collection<String> values) {
        ValueOutput.TypedOutputList<String> outputList = output.list(key, Codec.STRING);
        values.forEach(outputList::add);
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
        ValueOutput.TypedOutputList<UUID> outputList = output.list(key, UUIDUtil.CODEC);
        values.forEach(outputList::add);
    }

    @Override
    public void putItemStacks(String key, List<ItemStack> values) {
        ValueOutput.TypedOutputList<ItemStack> outputList = output.list(key, ItemStack.OPTIONAL_CODEC);
        values.forEach(outputList::add);
    }

    @Override
    public DataWriter child(String key) {
        return new ValueOutputDataWriter(output.child(key));
    }

    @Override
    public DataWriter addChild(String key) {
        return new ValueOutputDataWriter(output.childrenList(key).addChild());
    }
}
