package com.vikingkittens.mc.customers.compatability.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

final class CompoundTagDataReader extends ValueInputDataReader {
    CompoundTagDataReader(CompoundTag tag) {
        this(tag, RegistryAccess.EMPTY);
    }

    CompoundTagDataReader(CompoundTag tag, HolderLookup.Provider registries) {
        super(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }
}
