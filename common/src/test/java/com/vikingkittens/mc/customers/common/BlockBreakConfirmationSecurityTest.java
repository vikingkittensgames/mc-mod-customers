package com.vikingkittens.mc.customers.common;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockBreakConfirmationSecurityTest {
    @Test
    void acceptsConfirmationWithinNormalBlockInteractionRange() {
        assertTrue(BlockBreakConfirmation.isWithinConfirmationRange(
                new Vec3(0.5D, 0.5D, 6.5D),
                BlockPos.ZERO
        ));
    }

    @Test
    void rejectsConfirmationAfterPlayerMovesOutOfRange() {
        assertFalse(BlockBreakConfirmation.isWithinConfirmationRange(
                new Vec3(0.5D, 0.5D, 6.51D),
                BlockPos.ZERO
        ));
    }
}
