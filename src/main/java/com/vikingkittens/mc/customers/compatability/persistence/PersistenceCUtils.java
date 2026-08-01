package com.vikingkittens.mc.customers.compatability.persistence;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Creates shared persistence interfaces from version-specific storage objects.
 */
public final class PersistenceCUtils {
    private PersistenceCUtils() {
    }

    public static DataReader reader(ValueInput input) {
        return new ValueInputDataReader(input);
    }

    public static DataWriter writer(ValueOutput output) {
        return new ValueOutputDataWriter(output);
    }
}
