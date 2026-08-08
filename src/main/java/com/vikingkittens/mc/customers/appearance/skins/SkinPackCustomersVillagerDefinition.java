package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record SkinPackCustomersVillagerDefinition(String name, List<ResourceLocation> skins) {
    public static final Codec<SkinPackCustomersVillagerDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(SkinPackCustomersVillagerDefinition::name),
                    ResourceLocation.CODEC.listOf().fieldOf("skins").forGetter(SkinPackCustomersVillagerDefinition::skins)
            ).apply(instance, SkinPackCustomersVillagerDefinition::new));

    public SkinPackCustomersVillagerDefinition {
        skins = List.copyOf(skins);
    }

    public Component getName() {
        return Component.literal(name);
    }
}
