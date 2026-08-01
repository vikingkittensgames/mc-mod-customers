package com.vikingkittens.mc.customers.client.compatability;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
class RenderingCUtilsTest {
    @Test
    void createsSixBoxFaces() {
        AABB bounds = new AABB(
                3.75D,
                4.75D,
                5.75D,
                4.25D,
                5.25D,
                6.25D
        );

        List<Vec3[]> faces =
                RenderingCUtils.getBoxFaces(bounds);

        assertEquals(6, faces.size());
        assertTrue(faces.stream().allMatch(face -> face.length == 4));
        assertTrue(faces.stream()
                .flatMap(Arrays::stream)
                .allMatch(vertex ->
                        (vertex.x() == bounds.minX
                                || vertex.x() == bounds.maxX)
                                && (vertex.y() == bounds.minY
                                || vertex.y() == bounds.maxY)
                                && (vertex.z() == bounds.minZ
                                || vertex.z() == bounds.maxZ)
                ));
    }
}
