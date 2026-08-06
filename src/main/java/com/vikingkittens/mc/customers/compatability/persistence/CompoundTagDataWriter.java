package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Adapts the shared persistence writer to Minecraft 1.21.1 compound tags.
 */
final class CompoundTagDataWriter implements DataWriter {
    private final CompoundTag tag;
    private final HolderLookup.Provider registries;
    private final Map<String, ListTag> childLists = new HashMap<>();

    CompoundTagDataWriter(CompoundTag tag) {
        this(tag, null);
    }

    CompoundTagDataWriter(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        this.tag = tag;
        this.registries = registries;
    }

    @Override
    public void putString(String key, String value) {
        tag.putString(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    @Override
    public void putBlockPos(String key, BlockPos value) {
        tag.put(key, NbtUtils.writeBlockPos(value));
    }

    @Override
    public void putBlockState(String key, BlockState value) {
        tag.put(key, NbtUtils.writeBlockState(value));
    }

    @Override
    public void putUuid(String key, UUID value) {
        tag.putUUID(key, value);
    }

    @Override
    public void putUuids(String key, Collection<UUID> values) {
        ListTag uuidTags = new ListTag();
        values.stream()
                .map(NbtUtils::createUUID)
                .forEach(uuidTags::add);
        tag.put(key, uuidTags);
    }

    @Override
    public void putItemStacks(String key, List<ItemStack> values) {
        ItemStackHandler inventory = new ItemStackHandler(values.size());
        for (int slot = 0; slot < values.size(); slot++) {
            inventory.setStackInSlot(slot, values.get(slot).copy());
        }
        tag.put(
                key,
                inventory.serializeNBT(
                        Objects.requireNonNull(registries)
                )
        );
    }

    @Override
    public DataWriter child(String key) {
        CompoundTag childTag = new CompoundTag();
        tag.put(key, childTag);
        return new CompoundTagDataWriter(childTag, registries);
    }

    @Override
    public DataWriter addChild(String key) {
        ListTag childList = childLists.computeIfAbsent(key, ignored -> {
            ListTag newList = new ListTag();
            tag.put(key, newList);
            return newList;
        });
        CompoundTag childTag = new CompoundTag();
        childList.add(childTag);
        return new CompoundTagDataWriter(childTag, registries);
    }
}
