package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerPoint;

final class SkinCustomersVillagerGeckoGeometry {
    private static final float MODEL_UNITS_PER_BLOCK = 16.0F;
    private static final SkinCustomersVillagerPoint CANONICAL_SITTING_PIVOT =
            new SkinCustomersVillagerPoint(0.0F, 12.0F, 0.0F);

    private SkinCustomersVillagerGeckoGeometry() {}

    static Vec3 sittingOffset(SkinCustomersVillagerPoint pivot, float scale, float bodyYawDegrees) {
        Vec3 localOffset = new Vec3(
                (CANONICAL_SITTING_PIVOT.x() - pivot.x()) * scale / MODEL_UNITS_PER_BLOCK,
                (CANONICAL_SITTING_PIVOT.y() - pivot.y()) * scale / MODEL_UNITS_PER_BLOCK,
                (CANONICAL_SITTING_PIVOT.z() - pivot.z()) * scale / MODEL_UNITS_PER_BLOCK
        );
        return localOffset.yRot(-bodyYawDegrees * Mth.DEG_TO_RAD);
    }

    static float trackingRotation(float degrees, float multiplier, float maximumDegrees) {
        return Mth.clamp(degrees * multiplier, -maximumDegrees, maximumDegrees) * Mth.DEG_TO_RAD;
    }
}
