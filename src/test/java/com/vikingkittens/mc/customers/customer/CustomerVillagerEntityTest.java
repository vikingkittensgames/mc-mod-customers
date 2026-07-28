package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies customer entity state persistence.
 */
class CustomerVillagerEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    /**
     * Verifies that a selected counter target survives entity save and load.
     */
    @Test
    void roundTripsCounterTargetBlockPosition() {
        BlockPos targetPosition = new BlockPos(10, 64, -20);
        CompoundTag tag = new CompoundTag();
        CustomerVillagerEntity.saveCounterTargetBlockPos(tag, targetPosition);

        assertEquals(
                targetPosition,
                CustomerVillagerEntity.readCounterTargetBlockPos(tag).orElseThrow()
        );
    }
}
