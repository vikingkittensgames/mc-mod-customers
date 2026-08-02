package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void routesSneakingInteractionToThePickupCounter() {
        PlayerInteractEvent.RightClickBlock event =
                mock(PlayerInteractEvent.RightClickBlock.class);
        Player player = mock(Player.class);
        Level level = mock(Level.class);
        BlockState state = mock(BlockState.class);
        CustomerPickupCounterBlock block =
                mock(CustomerPickupCounterBlock.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getLevel()).thenReturn(level);
        when(event.getPos()).thenReturn(BlockPos.ZERO);
        when(player.isSecondaryUseActive()).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(state);
        when(state.getBlock()).thenReturn(block);

        CustomerEvents.onPickupCounterInteract(event);

        verify(event).setUseBlock(TriState.TRUE);
        verify(event).setUseItem(TriState.FALSE);
    }
}
