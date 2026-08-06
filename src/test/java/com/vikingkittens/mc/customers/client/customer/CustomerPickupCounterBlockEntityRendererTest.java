package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerPickupCounterBlockEntityRendererTest {
    @Test
    void placesTheOldestItemInTheCenter() {
        List<Vec3> positions =
                CustomerPickupCounterBlockEntityRenderer
                        .getItemPositions(9);

        assertEquals(new Vec3(0.5D, 0.08D, 0.5D), positions.get(0));
    }

    @Test
    void arrangesNineItemsInAThreeByThreeGrid() {
        assertEquals(
                List.of(
                        new Vec3(0.5D, 0.08D, 0.5D),
                        new Vec3(0.25D, 0.08D, 0.25D),
                        new Vec3(0.5D, 0.082D, 0.25D),
                        new Vec3(0.75D, 0.08D, 0.25D),
                        new Vec3(0.25D, 0.082D, 0.5D),
                        new Vec3(0.75D, 0.082D, 0.5D),
                        new Vec3(0.25D, 0.08D, 0.75D),
                        new Vec3(0.5D, 0.082D, 0.75D),
                        new Vec3(0.75D, 0.08D, 0.75D)
                ),
                CustomerPickupCounterBlockEntityRenderer
                        .getItemPositions(9)
        );
    }

    @Test
    void returnsOnlyPositionsNeededForStoredItems() {
        assertEquals(
                3,
                CustomerPickupCounterBlockEntityRenderer
                        .getItemPositions(3)
                        .size()
        );
    }
    @Test
    void rendersAdditionalModelsAsTheStackFills() {
        assertEquals(
                1,
                CustomerPickupCounterBlockEntityRenderer
                        .getModelCount(1, 64)
        );
        assertEquals(
                2,
                CustomerPickupCounterBlockEntityRenderer
                        .getModelCount(2, 64)
        );
        assertEquals(
                3,
                CustomerPickupCounterBlockEntityRenderer
                        .getModelCount(32, 64)
        );
        assertEquals(
                5,
                CustomerPickupCounterBlockEntityRenderer
                        .getModelCount(64, 64)
        );
    }
}
