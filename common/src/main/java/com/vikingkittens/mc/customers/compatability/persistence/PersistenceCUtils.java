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

    public static DataReader reader(
            CompoundTag tag,
            HolderLookup.Provider ignored
    ) {
        return reader(tag);
    }

    public static DataWriter writer(
            CompoundTag tag,
            HolderLookup.Provider ignored
    ) {
        return writer(tag);
    }
}
