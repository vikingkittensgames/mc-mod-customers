package com.vikingkittens.mc.customers.client.appearance.skins;

record SkinCustomersVillagerGeckoHeadRotation(
        float animatedPitch,
        float animatedYaw,
        float renderedPitch,
        float renderedYaw
) {
    static SkinCustomersVillagerGeckoHeadRotation create(
            float currentPitch,
            float currentYaw,
            float initialPitch,
            float initialYaw,
            boolean animatedThisFrame,
            float trackingPitch,
            float trackingYaw
    ) {
        float animatedPitch = animatedThisFrame ? currentPitch : initialPitch;
        float animatedYaw = animatedThisFrame ? currentYaw : initialYaw;
        return new SkinCustomersVillagerGeckoHeadRotation(
                animatedPitch,
                animatedYaw,
                animatedPitch + trackingPitch,
                animatedYaw + trackingYaw
        );
    }
}
