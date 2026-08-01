package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
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
        DataWriter output = mock(DataWriter.class);
        CustomerVillagerEntity.saveCounterTargetBlockPos(output, targetPosition);
        verify(output).putBlockPos("CounterTargetBlockPos", targetPosition);

        DataReader input = mock(DataReader.class);
        when(input.getBlockPos("CounterTargetBlockPos"))
                .thenReturn(java.util.Optional.of(targetPosition));

        assertEquals(
                targetPosition,
                CustomerVillagerEntity.readCounterTargetBlockPos(input).orElseThrow()
        );
    }
}
