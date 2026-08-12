package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerInteractionsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void usesPickupCounterWhenPlayerIsSneaking() {
        Player player = mock(Player.class);
        Level level = mock(Level.class);
        BlockState state = mock(BlockState.class);
        when(player.isSecondaryUseActive()).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(state);
        when(state.getBlock()).thenReturn(mock(CustomerPickupCounterBlock.class));

        assertTrue(CustomerInteractions.shouldUsePickupCounter(player, level, BlockPos.ZERO));
    }

    @Test
    void doesNotUsePickupCounterWhenPlayerIsNotSneaking() {
        Player player = mock(Player.class);

        assertFalse(CustomerInteractions.shouldUsePickupCounter(player, mock(Level.class), BlockPos.ZERO));
    }

    @Test
    void doesNotUseAnotherBlockWhenPlayerIsSneaking() {
        Player player = mock(Player.class);
        Level level = mock(Level.class);
        BlockState state = mock(BlockState.class);
        when(player.isSecondaryUseActive()).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(state);
        when(state.getBlock()).thenReturn(mock(Block.class));

        assertFalse(CustomerInteractions.shouldUsePickupCounter(player, level, BlockPos.ZERO));
    }
}
