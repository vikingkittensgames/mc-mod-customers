package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        ValueOutput output = mock(ValueOutput.class);
        CustomerVillagerEntity.saveCounterTargetBlockPos(
                output,
                targetPosition
        );
        verify(output).store(
                "CounterTargetBlockPos",
                BlockPos.CODEC,
                targetPosition
        );

        ValueInput input = mock(ValueInput.class);
        when(input.read("CounterTargetBlockPos", BlockPos.CODEC))
                .thenReturn(java.util.Optional.of(targetPosition));

        assertEquals(
                targetPosition,
                CustomerVillagerEntity.readCounterTargetBlockPos(input)
                        .orElseThrow()
        );
    }
}
