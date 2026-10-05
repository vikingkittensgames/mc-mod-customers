package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.compatability.ComponentCUtils;

public record SkinCustomersVillagerDefinition(
        ResourceLocation texture,
        SkinCustomersVillagerModel model,
        boolean legacy,
        float scale,
        float shadowRadius,
        float nameTagOffset,
        List<Component> names,
        Map<String, ResourceLocation> sounds,
        Optional<ResourceLocation> animation,
        Optional<String> headBone,
        Optional<SkinCustomersVillagerHeadTracking> headTracking,
        Optional<SkinCustomersVillagerPoint> sittingPivot,
        Map<String, String> animations,
        List<String> requiredMods
) {
    public static final float DEFAULT_SCALE = 0.9375F;
    public static final float DEFAULT_SHADOW_RADIUS = 0.5F;
    private static final Set<String> REQUIRED_GECKO_ANIMATIONS = Set.of("idle", "walk", "sit");
    private static final Codec<SkinCustomersVillagerDefinition> UNVALIDATED_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(SkinCustomersVillagerDefinition::texture),
                    SkinCustomersVillagerModel.CODEC.optionalFieldOf("model", SkinCustomersVillagerModel.WIDE)
                            .forGetter(SkinCustomersVillagerDefinition::model),
                    Codec.BOOL.optionalFieldOf("legacy", false).forGetter(SkinCustomersVillagerDefinition::legacy),
                    Codec.floatRange(0.01F, 16.0F).optionalFieldOf("scale", DEFAULT_SCALE)
                            .forGetter(SkinCustomersVillagerDefinition::scale),
                    Codec.floatRange(0.0F, 16.0F).optionalFieldOf("shadow_radius", DEFAULT_SHADOW_RADIUS)
                            .forGetter(SkinCustomersVillagerDefinition::shadowRadius),
                    Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("name_tag_offset", 0.0F)
                            .forGetter(SkinCustomersVillagerDefinition::nameTagOffset),
                    ComponentCUtils.codec().listOf().optionalFieldOf("names", List.of())
                            .forGetter(SkinCustomersVillagerDefinition::names),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("sounds", Map.of())
                            .forGetter(SkinCustomersVillagerDefinition::sounds),
                    ResourceLocation.CODEC.optionalFieldOf("animation")
                            .forGetter(SkinCustomersVillagerDefinition::animation),
                    Codec.STRING.optionalFieldOf("head_bone").forGetter(SkinCustomersVillagerDefinition::headBone),
                    SkinCustomersVillagerHeadTracking.CODEC.optionalFieldOf("head_tracking")
                            .forGetter(SkinCustomersVillagerDefinition::headTracking),
                    SkinCustomersVillagerPoint.CODEC.optionalFieldOf("sitting_pivot")
                            .forGetter(SkinCustomersVillagerDefinition::sittingPivot),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("animations", Map.of())
                            .forGetter(SkinCustomersVillagerDefinition::animations),
                    Codec.STRING.listOf().optionalFieldOf("required_mods", List.of())
                            .forGetter(SkinCustomersVillagerDefinition::requiredMods)
            ).apply(instance, SkinCustomersVillagerDefinition::new)
    );
    public static final Codec<SkinCustomersVillagerDefinition> CODEC = UNVALIDATED_CODEC.flatXmap(
            SkinCustomersVillagerDefinition::validate,
            SkinCustomersVillagerDefinition::validate
    );

    public SkinCustomersVillagerDefinition {
        names = List.copyOf(names);
        sounds = Map.copyOf(sounds);
        animations = Map.copyOf(animations);
        requiredMods = List.copyOf(requiredMods);
    }

    public Optional<ResourceLocation> getSound(SkinCustomersVillagerSound sound) {
        return Optional.ofNullable(sounds.get(sound.getSerializedName()));
    }

    public ResourceLocation getTextureLocation() {
        if (texture.getPath().endsWith(".png")) {
            return texture;
        }
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "textures/customers/skins/" + texture.getPath() + ".png");
    }

    public String getHeadBone() {
        return headBone.orElse("head");
    }

    public SkinCustomersVillagerHeadTracking getHeadTracking() {
        return headTracking.orElse(SkinCustomersVillagerHeadTracking.DEFAULT);
    }

    private static DataResult<SkinCustomersVillagerDefinition> validate(SkinCustomersVillagerDefinition definition) {
        if (definition.requiredMods().stream().anyMatch(String::isBlank)) {
            return DataResult.error(() -> "Required mod IDs cannot be blank");
        }
        boolean hasGeckoProperties = definition.animation().isPresent()
                || definition.headBone().isPresent()
                || definition.headTracking().isPresent()
                || definition.sittingPivot().isPresent()
                || !definition.animations().isEmpty();
        if (!definition.model().isGecko()) {
            return hasGeckoProperties
                    ? DataResult.error(() -> "Gecko skin properties require a namespaced model resource ID")
                    : DataResult.success(definition);
        }
        if (definition.animation().isEmpty()) {
            return DataResult.error(() -> "Gecko skin definition is missing animation");
        }
        if (definition.sittingPivot().isEmpty()) {
            return DataResult.error(() -> "Gecko skin definition is missing sitting_pivot");
        }
        for (String animation : REQUIRED_GECKO_ANIMATIONS) {
            if (definition.animations().getOrDefault(animation, "").isBlank()) {
                return DataResult.error(() -> "Gecko skin definition is missing animations." + animation);
            }
        }
        if (definition.animations().values().stream().anyMatch(String::isBlank)) {
            return DataResult.error(() -> "Gecko animation names cannot be blank");
        }
        return DataResult.success(definition);
    }
}
