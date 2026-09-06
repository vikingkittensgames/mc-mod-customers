package com.vikingkittens.mc.customers.compatability.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Creates shared persistence interfaces from version-specific storage objects.
 */
public final class PersistenceCUtils {
    private PersistenceCUtils() {
    }

    public static DataReader reader(CompoundTag tag) {
        return new CompoundTagDataReader(tag);
    }

    public static DataWriter writer(CompoundTag tag) {
        return new CompoundTagDataWriter(tag);
    }

    public static DataReader reader(ValueInput input) {
        return new ValueInputDataReader(input);
    }

    public static DataWriter writer(ValueOutput output) {
        return new ValueOutputDataWriter(output);
    }
    /**
     * Creates a reader capable of decoding registry-backed values.
     *
     * @param tag source compound
     * @param registries registry provider
     * @return shared persistence reader
     */
    public static DataReader reader(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        return new CompoundTagDataReader(tag, registries);
    }

    /**
     * Creates a writer capable of encoding registry-backed values.
     *
     * @param tag target compound
     * @param registries registry provider
     * @return shared persistence writer
     */
    public static DataWriter writer(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        return new CompoundTagDataWriter(tag, registries);
    }
}
