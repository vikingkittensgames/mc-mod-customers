package com.vikingkittens.mc.customers.customer;

import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSeatEntityTest {
    private static final double POSITION_TOLERANCE = 0.000001D;
    private static final Vec3 SEAT_POSITION =
            new Vec3(10.5D, 64.0D, 20.5D);
    @Test
    void alignsCustomerHipWithSeatSurface() {
        assertEquals(
                new Vec3(0.0D, 0.7D, 0.0D),
                CustomerSeatLogic.getCustomerVehicleAttachmentPoint()
        );
    }
    @Test
    void ignoresExperienceOrbsWhenFindingSeatPosition() {
        assertTrue(CustomerSeatLogic.isIgnoredSeatPositionEntityType(
                ExperienceOrb.class
        ));
    }

    @Test
    void ignoresDroppedItemsWhenFindingSeatPosition() {
        assertTrue(CustomerSeatLogic.isIgnoredSeatPositionEntityType(
                ItemEntity.class
        ));
    }

    @Test
    void retainsOtherEntitiesWhenFindingSeatPosition() {
        assertFalse(CustomerSeatLogic.isIgnoredSeatPositionEntityType(
                Entity.class
        ));
    }

    @Test
    void positionsPassengerFromFixedSeatPosition() {
        assertEquals(
                new Vec3(10.5D, 64.0D, 20.5D),
                CustomerSeatLogic.getPassengerPosition(
                        new Vec3(10.5D, 64.7D, 20.5D),
                        new Vec3(0.0D, 0.7D, 0.0D)
                )
        );
    }

    @Test
    void dismountsBehindWhenBehindIsAir() {
        assertDismountLocation(
                new Vec3(10.5D, 64.0D, 19.5D),
                0.0F,
                Set.of(new BlockPos(10, 64, 19))
        );
    }

    @Test
    void dismountsLeftWhenBehindIsBlocked() {
        assertDismountLocation(
                new Vec3(11.5D, 64.0D, 20.5D),
                0.0F,
                Set.of(new BlockPos(11, 64, 20))
        );
    }

    @Test
    void dismountsRightWhenBehindAndLeftAreBlocked() {
        assertDismountLocation(
                new Vec3(9.5D, 64.0D, 20.5D),
                0.0F,
                Set.of(new BlockPos(9, 64, 20))
        );
    }

    @Test
    void usesSeatPositionWhenEveryDirectionIsBlocked() {
        assertDismountLocation(SEAT_POSITION, 0.0F, Set.of());
    }

    @Test
    void calculatesFallbacksRelativeToInitialRotation() {
        assertDismountLocation(
                new Vec3(11.5D, 64.0D, 20.5D),
                90.0F,
                Set.of(new BlockPos(11, 64, 20))
        );
    }

    private static void assertDismountLocation(
            Vec3 expected,
            float initialYRotation,
            Set<BlockPos> airPositions
    ) {
        Vec3 actual = CustomerSeatLogic.getDismountLocation(
                SEAT_POSITION,
                initialYRotation,
                airPositions::contains
        );
        assertEquals(expected.x, actual.x, POSITION_TOLERANCE);
        assertEquals(expected.y, actual.y, POSITION_TOLERANCE);
        assertEquals(expected.z, actual.z, POSITION_TOLERANCE);
    }

    @Test
    void recognizesSeatNames() {
        assertTrue(CustomerSeatLogic.isSeatName("oak_seat"));
        assertTrue(CustomerSeatLogic.isSeatName("dining_chair"));
        assertTrue(CustomerSeatLogic.isSeatName("bar_stool"));
        assertTrue(CustomerSeatLogic.isSeatName("park_bench"));
        assertTrue(CustomerSeatLogic.isSeatName("red_cushion"));
        assertFalse(CustomerSeatLogic.isSeatName("stone"));
    }

    @Test
    void recognizesSeatHeightWithinMiddleThird() {
        assertFalse(CustomerSeatLogic.isSeatHeight(0.25));
        assertTrue(CustomerSeatLogic.isSeatHeight(1.0 / 3.0));
        assertTrue(CustomerSeatLogic.isSeatHeight(0.5));
        assertTrue(CustomerSeatLogic.isSeatHeight(2.0 / 3.0));
        assertFalse(CustomerSeatLogic.isSeatHeight(0.75));
    }

    @Test
    void usesLargestTopSurfaceForComplexShape() {
        assertEquals(
                0.5,
                CustomerSeatLogic.getBestSeatHeight(
                        Shapes.or(
                                Shapes.box(0, 0, 0, 1, 0.5, 1),
                                Shapes.box(0, 0.5, 0.75, 1, 1, 1)
                        )
                )
        );
    }

    @Test
    void emptyShapeDefaultsToBlockCenter() {
        assertEquals(
                0.5,
                CustomerSeatLogic.getBestSeatHeight(Shapes.empty())
        );
    }

    @Test
    void emptySeatExpiresAfterTwoMinutes() {
        assertFalse(CustomerSeatLogic.shouldDiscardEmptySeat(2_399));
        assertTrue(CustomerSeatLogic.shouldDiscardEmptySeat(2_400));
    }
}
