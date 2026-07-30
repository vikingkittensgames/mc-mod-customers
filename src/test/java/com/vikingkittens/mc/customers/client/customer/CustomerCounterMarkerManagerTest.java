package com.vikingkittens.mc.customers.client.customer;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerCounterMarkerManagerTest {
    @Test
    void createsSixFacesForFilledBox() {
        Vec3 center = new Vec3(4.0D, 5.0D, 6.0D);

        List<Vec3[]> faces = CustomerCounterMarkerRenderer.getBoxFaces(center, 0.5D);

        assertEquals(6, faces.size());
        assertTrue(faces.stream().allMatch(face -> face.length == 4));
        assertTrue(faces.stream()
                .flatMap(face -> Arrays.stream(face))
                .allMatch(vertex ->
                        (vertex.x() == 3.75D || vertex.x() == 4.25D)
                                && (vertex.y() == 4.75D || vertex.y() == 5.25D)
                                && (vertex.z() == 5.75D || vertex.z() == 6.25D)
                ));
    }
    @AfterEach
    void clearMarkers() {
        CustomerCounterMarkerManager.clear();
    }

    @Test
    void markersRemainVisibleUntilNinetySecondsExpire() {
        CustomerCounterMarker marker = new CustomerCounterMarker(
                BlockPos.ZERO,
                CustomerSpawnerMode.DAY
        );
        BlockPos surroundingPosition = new BlockPos(1, 2, 3);

        CustomerCounterMarkerManager.show(
                List.of(marker),
                List.of(surroundingPosition),
                1_000L
        );

        assertEquals(
                List.of(marker),
                CustomerCounterMarkerManager.get(90_999L)
        );
        assertEquals(
                List.of(surroundingPosition),
                CustomerCounterMarkerManager.getSurroundingPositions(90_999L)
        );
        assertEquals(
                List.of(),
                CustomerCounterMarkerManager.get(91_000L)
        );
        assertEquals(
                List.of(),
                CustomerCounterMarkerManager.getSurroundingPositions(91_000L)
        );
    }

    @Test
    void showingMarkersReplacesThePreviousSetAndRestartsExpiration() {
        CustomerCounterMarker first = new CustomerCounterMarker(
                BlockPos.ZERO,
                CustomerSpawnerMode.DAY
        );
        CustomerCounterMarker second = new CustomerCounterMarker(
                new BlockPos(10, 20, 30),
                CustomerSpawnerMode.NIGHT
        );

        CustomerCounterMarkerManager.show(List.of(first), List.of(), 1_000L);
        CustomerCounterMarkerManager.show(List.of(second), List.of(), 50_000L);

        assertEquals(
                List.of(second),
                CustomerCounterMarkerManager.get(139_999L)
        );
    }

    @Test
    void rotationAdvancesSmoothlyBetweenGameTicks() {
        assertEquals(
                80.0F,
                CustomerCounterMarkerManager.getRotationDegrees(1_000L),
                0.0001F
        );
        assertEquals(
                80.8F,
                CustomerCounterMarkerManager.getRotationDegrees(1_010L),
                0.0001F
        );
    }

    @Test
    void rotationWrapsAfterOneCompleteTurn() {
        assertEquals(
                359.92F,
                CustomerCounterMarkerManager.getRotationDegrees(4_499L),
                0.0001F
        );
        assertEquals(
                0.0F,
                CustomerCounterMarkerManager.getRotationDegrees(4_500L),
                0.0001F
        );
    }

    @Test
    void bobbingUsesElapsedClientTime() {
        assertEquals(
                0.0F,
                CustomerCounterMarkerManager.getBobOffset(0L),
                0.0001F
        );
        assertEquals(
                0.1F,
                CustomerCounterMarkerManager.getBobOffset(785L),
                0.001F
        );
    }
}
