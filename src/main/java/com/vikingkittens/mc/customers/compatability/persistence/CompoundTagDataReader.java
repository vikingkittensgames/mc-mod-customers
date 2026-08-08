package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Adapts Minecraft 1.21.1 compound tags to the shared persistence reader.
 */
final class CompoundTagDataReader implements DataReader {
    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    CompoundTagDataReader(CompoundTag tag) {
        this(tag, null);
    }

    CompoundTagDataReader(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        this.tag = tag;
        this.registries = registries;
    }

    @Override
    public Optional<String> getString(String key) {
        return tag.contains(key, Tag.TAG_STRING)
                ? Optional.of(tag.getString(key))
                : Optional.empty();
    }

    @Override
    public Optional<Float> getFloat(String key) {
        return tag.contains(key, Tag.TAG_FLOAT)
                ? Optional.of(tag.getFloat(key))
                : Optional.empty();
    }

    @Override
    public Optional<Integer> getInt(String key) {
        return tag.contains(key, Tag.TAG_INT)
                ? Optional.of(tag.getInt(key))
                : Optional.empty();
    }

    @Override
    public List<String> getStrings(String key) {
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag stringTags = tag.getList(key, Tag.TAG_STRING);
        List<String> strings = new ArrayList<>(stringTags.size());
        for (Tag stringTag : stringTags) {
            strings.add(stringTag.getAsString());
        }
        return List.copyOf(strings);
    }

    @Override
    public boolean getBoolean(String key) {
        return tag.getBoolean(key);
    }

    @Override
    public Optional<BlockPos> getBlockPos(String key) {
        return NbtUtils.readBlockPos(tag, key);
    }

    @Override
    public Optional<BlockState> getBlockState(String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(NbtUtils.readBlockState(
                BuiltInRegistries.BLOCK.asLookup(),
                tag.getCompound(key)
        ));
    }

    @Override
    public Optional<UUID> getUuid(String key) {
        return tag.hasUUID(key)
                ? Optional.of(tag.getUUID(key))
                : Optional.empty();
    }

    @Override
    public List<UUID> getUuids(String key) {
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag uuidTags = tag.getList(key, Tag.TAG_INT_ARRAY);
        List<UUID> uuids = new ArrayList<>(uuidTags.size());
        for (Tag uuidTag : uuidTags) {
            uuids.add(NbtUtils.loadUUID(uuidTag));
        }
        return List.copyOf(uuids);
    }

    @Override
    public List<ItemStack> getItemStacks(String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return List.of();
        }
        ItemStackHandler inventory = new ItemStackHandler();
        inventory.deserializeNBT(
                Objects.requireNonNull(registries),
                tag.getCompound(key)
        );
        List<ItemStack> stacks =
                new ArrayList<>(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return List.copyOf(stacks);
    }

    @Override
    public DataReader childOrEmpty(String key) {
        return new CompoundTagDataReader(
                tag.getCompound(key),
                registries
        );
    }

    @Override
    public List<DataReader> getChildren(String key) {
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag childTags = tag.getList(key, Tag.TAG_COMPOUND);
        List<DataReader> children = new ArrayList<>(childTags.size());
        for (int index = 0; index < childTags.size(); index++) {
            children.add(new CompoundTagDataReader(
                    childTags.getCompound(index),
                    registries
            ));
        }
        return List.copyOf(children);
    }
}
