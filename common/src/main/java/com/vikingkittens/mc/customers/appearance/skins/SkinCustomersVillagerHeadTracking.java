package com.vikingkittens.mc.customers.appearance.skins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkinCustomersVillagerHeadTracking(
        boolean enabled,
        float maximumYaw,
        float maximumPitch,
        float yawMultiplier,
        float pitchMultiplier
) {
    public static final SkinCustomersVillagerHeadTracking DEFAULT = new SkinCustomersVillagerHeadTracking(
            true,
            60.0F,
            45.0F,
            1.0F,
            1.0F
    );
    public static final Codec<SkinCustomersVillagerHeadTracking> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true)
                            .forGetter(SkinCustomersVillagerHeadTracking::enabled),
                    Codec.floatRange(0.0F, 180.0F).optionalFieldOf("maximum_yaw", 60.0F)
                            .forGetter(SkinCustomersVillagerHeadTracking::maximumYaw),
                    Codec.floatRange(0.0F, 90.0F).optionalFieldOf("maximum_pitch", 45.0F)
                            .forGetter(SkinCustomersVillagerHeadTracking::maximumPitch),
                    Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("yaw_multiplier", 1.0F)
                            .forGetter(SkinCustomersVillagerHeadTracking::yawMultiplier),
                    Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("pitch_multiplier", 1.0F)
                            .forGetter(SkinCustomersVillagerHeadTracking::pitchMultiplier)
            ).apply(instance, SkinCustomersVillagerHeadTracking::new)
    );
}
