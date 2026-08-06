package com.vikingkittens.mc.customers.compatability.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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
