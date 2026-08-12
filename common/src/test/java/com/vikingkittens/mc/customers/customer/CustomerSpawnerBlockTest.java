package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.block.state.BlockBehaviour;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerSpawnerBlockTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void hasLogBreakingStrength() {
        BlockBehaviour.Properties properties =
                mock(BlockBehaviour.Properties.class);
        when(properties.strength(2.0F)).thenReturn(properties);

        BlockBehaviour.Properties result =
                CustomerSpawnerBlock.withLogStrength(properties);

        assertSame(properties, result);
        verify(properties).strength(2.0F);
    }
}
