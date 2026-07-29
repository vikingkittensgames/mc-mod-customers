package com.vikingkittens.mc.customers.common.ai;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MobMoveToGoalTest {
    /**
     * Initializes Minecraft registries required to mock entity classes.
     */
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    /**
     * Verifies movement goals leave jumping available to independent goals.
     */
    @Test
    void reservesMovementWithoutReservingJumping() {
        MobMoveToGoal goal = new MobMoveToGoal(
                mock(PathfinderMob.class),
                BlockPos.ZERO,
                0.5
        );

        assertEquals(EnumSet.of(Goal.Flag.MOVE), goal.getFlags());
    }

    @Test
    void scalesTimeoutWithDistance() {
        assertEquals(160, MobMoveToGoal.calculateMaxTicks(10, 0.25));
        assertEquals(1_024, MobMoveToGoal.calculateMaxTicks(64, 0.25));
    }

    @Test
    void accountsForMovementSpeed() {
        assertEquals(1_024, MobMoveToGoal.calculateMaxTicks(64, 0.25));
        assertEquals(256, MobMoveToGoal.calculateMaxTicks(64, 0.5));
    }

    @Test
    void calculatesPathLengthFromTheMobThroughEachRemainingNode() {
        assertEquals(
                17.0,
                MobMoveToGoal.calculatePathLength(
                        new Vec3(0, 0, 0),
                        List.of(
                                new Vec3(3, 0, 4),
                                new Vec3(3, 12, 4)
                        )
                )
        );
    }

    @Test
    void regeneratedPathDeadlineIncludesAlreadyElapsedTicks() {
        assertEquals(
                660,
                MobMoveToGoal.calculateDeadlineTicks(500, 10, 0.25)
        );
    }
}
