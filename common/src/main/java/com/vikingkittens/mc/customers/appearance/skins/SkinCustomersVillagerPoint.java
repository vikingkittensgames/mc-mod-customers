package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record SkinCustomersVillagerPoint(float x, float y, float z) {
    public static final Codec<SkinCustomersVillagerPoint> CODEC = Codec.FLOAT.listOf().comapFlatMap(
            SkinCustomersVillagerPoint::decode,
            point -> List.of(point.x(), point.y(), point.z())
    );

    private static DataResult<SkinCustomersVillagerPoint> decode(List<Float> values) {
        if (values.size() != 3) {
            return DataResult.error(() -> "Expected exactly three model coordinates");
        }
        return DataResult.success(new SkinCustomersVillagerPoint(values.get(0), values.get(1), values.get(2)));
    }
}
