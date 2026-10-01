package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
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

    @Test
    void opensConfigurationWhenHoldingAnOrdinaryItem() {
        CustomerSpawnerBlock block = mock(CustomerSpawnerBlock.class, CALLS_REAL_METHODS);
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerSpawnerBlockEntity spawner = mock(CustomerSpawnerBlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(spawner);

        InteractionResult result = block.useItemOn(
                new ItemStack(Items.BREAD),
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(InteractionResult.SUCCESS, result);
        verify(player).openMenu(spawner);
    }
}
