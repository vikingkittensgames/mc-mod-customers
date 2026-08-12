package com.vikingkittens.mc.customers.appearance.mca;

import java.util.Random;

public record McaCustomersVillagerVariation(
        boolean feminine,
        long randomSeed,
        float size,
        float width,
        float breast,
        float melanin,
        float hemoglobin,
        float eumelanin,
        float pheomelanin,
        float skin,
        float face,
        float voice,
        float voiceTone,
        float clothingChoice,
        float hairChoice,
        float hairDyeChoice
) {
    public static McaCustomersVillagerVariation fromSeed(
            float variationSeed
    ) {
        long randomSeed =
                Integer.toUnsignedLong(Float.floatToIntBits(variationSeed))
                        * 0x9E3779B97F4A7C15L;
        Random random = new Random(randomSeed);
        return new McaCustomersVillagerVariation(
                variationSeed >= 0.5F,
                randomSeed,
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat()
        );
    }
}
