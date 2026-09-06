package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;

final class CompoundTagDataWriter implements DataWriter {
    private final CompoundTag target;
    private final TagValueOutput rootOutput;
    private final ValueOutput output;
    private final String appendedChildrenKey;

    CompoundTagDataWriter(CompoundTag tag) {
        this(tag, RegistryAccess.EMPTY);
    }

    CompoundTagDataWriter(CompoundTag tag, HolderLookup.Provider registries) {
        this(
                tag,
                registries == RegistryAccess.EMPTY
                        ? TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING)
                        : TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries),
                null,
                null
        );
    }

    private CompoundTagDataWriter(
            CompoundTag target,
            TagValueOutput rootOutput,
            ValueOutput output,
            String appendedChildrenKey
    ) {
        this.target = target;
        this.rootOutput = rootOutput;
        this.output = output == null ? rootOutput : output;
        this.appendedChildrenKey = appendedChildrenKey;
    }

    @Override
    public void putString(String key, String value) {
        output.putString(key, value);
        commit();
    }

    @Override
    public void putFloat(String key, float value) {
        output.putFloat(key, value);
        commit();
    }

    @Override
    public void putInt(String key, int value) {
        output.putInt(key, value);
        commit();
    }

    @Override
    public void putStrings(String key, Collection<String> values) {
        ValueOutput.TypedOutputList<String> outputList = output.list(key, Codec.STRING);
        values.forEach(outputList::add);
        commit();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        output.putBoolean(key, value);
        commit();
    }

    @Override
    public void putBlockPos(String key, BlockPos value) {
        output.store(key, BlockPos.CODEC, value);
        commit();
    }

    @Override
    public void putBlockState(String key, BlockState value) {
        output.store(key, BlockState.CODEC, value);
        commit();
    }

    @Override
    public void putUuid(String key, UUID value) {
        output.store(key, UUIDUtil.CODEC, value);
        commit();
    }

    @Override
    public void putUuids(String key, Collection<UUID> values) {
        ValueOutput.TypedOutputList<UUID> outputList = output.list(key, UUIDUtil.CODEC);
        values.forEach(outputList::add);
        commit();
    }

    @Override
    public void putItemStacks(String key, List<ItemStack> values) {
        ValueOutput.TypedOutputList<ItemStack> outputList = output.list(key, ItemStack.OPTIONAL_CODEC);
        values.forEach(outputList::add);
        commit();
    }

    @Override
    public DataWriter child(String key) {
        return new CompoundTagDataWriter(target, rootOutput, output.child(key), null);
    }

    @Override
    public DataWriter addChild(String key) {
        return new CompoundTagDataWriter(target, rootOutput, output.childrenList(key).addChild(), key);
    }

    private void commit() {
        CompoundTag result = rootOutput.buildResult();
        if (appendedChildrenKey != null) {
            ListTag existing = target.getList(appendedChildrenKey).orElse(new ListTag());
            ListTag added = result.getList(appendedChildrenKey).orElse(new ListTag());
            for (int index = 0; index < added.size(); index++) {
                added.getCompound(index).ifPresent(existing::add);
            }
            result.remove(appendedChildrenKey);
            target.put(appendedChildrenKey, existing);
        }
        target.merge(result);
    }
}
