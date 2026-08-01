package com.vikingkittens.mc.customers.compatability.persistence;

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
}
