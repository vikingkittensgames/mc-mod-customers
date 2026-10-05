package com.vikingkittens.mc.customers.client.appearance.skins;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerGeckoHeadRotationTest {
    @Test
    void appliesTrackingOnTopOfRotationAnimatedThisFrame() {
        SkinCustomersVillagerGeckoHeadRotation rotation =
                SkinCustomersVillagerGeckoHeadRotation.create(0.25F, -0.5F, 0.0F, 0.0F, true, 0.5F, 1.0F);

        assertEquals(0.25F, rotation.animatedPitch());
        assertEquals(-0.5F, rotation.animatedYaw());
        assertEquals(0.75F, rotation.renderedPitch());
        assertEquals(0.5F, rotation.renderedYaw());
    }

    @Test
    void doesNotReuseTrackingRotationFromThePreviousFrame() {
        SkinCustomersVillagerGeckoHeadRotation first =
                SkinCustomersVillagerGeckoHeadRotation.create(0.0F, 0.0F, 0.0F, 0.0F, false, 0.5F, 1.0F);
        SkinCustomersVillagerGeckoHeadRotation second = SkinCustomersVillagerGeckoHeadRotation.create(
                first.renderedPitch(),
                first.renderedYaw(),
                0.0F,
                0.0F,
                false,
                0.5F,
                1.0F
        );

        assertEquals(0.5F, second.renderedPitch());
        assertEquals(1.0F, second.renderedYaw());
    }
}
