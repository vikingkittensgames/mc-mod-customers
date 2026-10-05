package com.vikingkittens.mc.customers.client.appearance.skins;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerGeckoGeometryTest {
    @Test
    void alignsScaledSittingPivotWithCanonicalHipPoint() {
        Vec3 offset = SkinCustomersVillagerGeckoGeometry.sittingOffset(
                new SkinCustomersVillagerPoint(0.0F, 10.0F, 0.0F),
                0.5F,
                0.0F
        );

        assertEquals(0.0D, offset.x, 0.0001D);
        assertEquals(0.0625D, offset.y, 0.0001D);
        assertEquals(0.0D, offset.z, 0.0001D);
    }

    @Test
    void rotatesHorizontalSittingCorrectionWithBody() {
        Vec3 offset = SkinCustomersVillagerGeckoGeometry.sittingOffset(
                new SkinCustomersVillagerPoint(2.0F, 12.0F, 0.0F),
                1.0F,
                90.0F
        );

        assertEquals(0.0D, offset.x, 0.0001D);
        assertEquals(0.0D, offset.y, 0.0001D);
        assertEquals(-0.125D, offset.z, 0.0001D);
    }

    @Test
    void limitsAndConvertsHeadTrackingRotation() {
        assertEquals(
                Math.toRadians(45.0D),
                SkinCustomersVillagerGeckoGeometry.trackingRotation(50.0F, 2.0F, 45.0F),
                0.0001D
        );
        assertEquals(
                Math.toRadians(-20.0D),
                SkinCustomersVillagerGeckoGeometry.trackingRotation(-40.0F, 0.5F, 45.0F),
                0.0001D
        );
    }
}
