package com.vikingkittens.mc.customers.common;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record SpawnerPresetDefinition(String name, Optional<ResourceLocation> icon, CompoundTag settings) {
    public static final Codec<SpawnerPresetDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(SpawnerPresetDefinition::name),
            ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(SpawnerPresetDefinition::icon),
            CompoundTag.CODEC.fieldOf("settings").forGetter(SpawnerPresetDefinition::settings)
    ).apply(instance, SpawnerPresetDefinition::new));
}
