package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Adapts Minecraft 1.21.11 value input to the shared persistence reader.
 */
final class ValueInputDataReader implements DataReader {
    private final ValueInput input;

    ValueInputDataReader(ValueInput input) {
        this.input = input;
    }

    @Override
    public Optional<String> getString(String key) {
        return input.getString(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return input.getBooleanOr(key, false);
    }

    @Override
    public Optional<BlockPos> getBlockPos(String key) {
        return input.read(key, BlockPos.CODEC);
    }

    @Override
    public Optional<BlockState> getBlockState(String key) {
        return input.read(key, BlockState.CODEC);
    }

    @Override
    public Optional<UUID> getUuid(String key) {
        return input.read(key, UUIDUtil.CODEC);
    }

    @Override
    public List<UUID> getUuids(String key) {
        return input.listOrEmpty(key, UUIDUtil.CODEC).stream().toList();
    }

    @Override
    public DataReader childOrEmpty(String key) {
        return new ValueInputDataReader(input.childOrEmpty(key));
    }

    @Override
    public List<DataReader> getChildren(String key) {
        return input.childrenListOrEmpty(key).stream()
                .map(ValueInputDataReader::new)
                .map(DataReader.class::cast)
                .toList();
    }
}