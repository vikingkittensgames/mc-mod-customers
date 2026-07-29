package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Verifies customer entity state persistence.
 */
class CustomerVillagerEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void ignoresFallDamage() {
        CustomerVillagerEntity customer = mock(
                CustomerVillagerEntity.class,
                CALLS_REAL_METHODS
        );

        assertFalse(customer.causeFallDamage(
                100.0F,
                1.0F,
                mock(DamageSource.class)
        ));
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
