package com.vikingkittens.mc.customers.appearance.skins;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record SkinCustomersVillagerDefinition(
        ResourceLocation texture,
        SkinCustomersVillagerModel model,
        boolean legacy,
        float scale,
        float shadowRadius,
        float nameTagOffset,
        Map<String, ResourceLocation> sounds
) {
    public static final float DEFAULT_SCALE = 0.9375F;
    public static final float DEFAULT_SHADOW_RADIUS = 0.5F;

    public static final Codec<SkinCustomersVillagerDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(SkinCustomersVillagerDefinition::texture),
                    SkinCustomersVillagerModel.CODEC.optionalFieldOf("model", SkinCustomersVillagerModel.WIDE).forGetter(SkinCustomersVillagerDefinition::model),
                    Codec.BOOL.optionalFieldOf("legacy", false).forGetter(SkinCustomersVillagerDefinition::legacy),
                    Codec.floatRange(0.01F, 16.0F).optionalFieldOf("scale", DEFAULT_SCALE).forGetter(SkinCustomersVillagerDefinition::scale),
                    Codec.floatRange(0.0F, 16.0F).optionalFieldOf("shadow_radius", DEFAULT_SHADOW_RADIUS).forGetter(SkinCustomersVillagerDefinition::shadowRadius),
                    Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("name_tag_offset", 0.0F).forGetter(SkinCustomersVillagerDefinition::nameTagOffset),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("sounds", Map.of()).forGetter(SkinCustomersVillagerDefinition::sounds)
            ).apply(instance, SkinCustomersVillagerDefinition::new));

    public SkinCustomersVillagerDefinition {
        sounds = Map.copyOf(sounds);
    }

    public Optional<ResourceLocation> getSound(SkinCustomersVillagerSound sound) {
        return Optional.ofNullable(sounds.get(sound.getSerializedName()));
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "textures/customers/skins/" + texture.getPath() + ".png");
    }
}
