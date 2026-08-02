package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterBlockTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void occupiesOnePixelAtTheBottomOfTheBlock() {
        CustomerPickupCounterBlock block = createBlock();
        VoxelShape shape = block.getShape(
                mock(BlockState.class),
                mock(BlockGetter.class),
                BlockPos.ZERO,
                mock(CollisionContext.class)
        );

        assertEquals(0.0D, shape.bounds().minX);
        assertEquals(0.0D, shape.bounds().minY);
        assertEquals(0.0D, shape.bounds().minZ);
        assertEquals(1.0D, shape.bounds().maxX);
        assertEquals(1.0D / 16.0D, shape.bounds().maxY);
        assertEquals(1.0D, shape.bounds().maxZ);
    }

    @Test
    void insertsHeldStackOnTheServer() {
        CustomerPickupCounterBlock block = createBlock();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack held = new ItemStack(Items.BREAD, 25);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(counter);
        when(counter.insertStackConnected(held)).thenReturn(ItemStack.EMPTY);

        ItemInteractionResult result = block.useItemOn(
                held,
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(ItemInteractionResult.SUCCESS, result);
        verify(counter).insertStackConnected(held);
        verify(player).setItemInHand(
                InteractionHand.MAIN_HAND,
                ItemStack.EMPTY
        );
    }

    @Test
    void insertsOneItemWhenThePlayerIsSneaking() {
        CustomerPickupCounterBlock block = createBlock();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack held = new ItemStack(Items.BREAD, 25);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(counter);
        when(player.isShiftKeyDown()).thenReturn(true);
        when(counter.insertStackConnected(any(ItemStack.class)))
                .thenReturn(ItemStack.EMPTY);

        ItemInteractionResult result = block.useItemOn(
                held,
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(ItemInteractionResult.SUCCESS, result);
        verify(counter).insertStackConnected(argThat(
                inserted -> inserted.is(Items.BREAD)
                        && inserted.getCount() == 1
        ));
        verify(player).setItemInHand(
                eq(InteractionHand.MAIN_HAND),
                argThat(remainder -> remainder.is(Items.BREAD)
                        && remainder.getCount() == 24)
        );
    }

    @Test
    void givesTheOldestStackToASneakingEmptyHand() {
        CustomerPickupCounterBlock block = createBlock();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack oldest = new ItemStack(Items.BREAD, 25);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(counter);
        when(counter.removeOldestConnected()).thenReturn(oldest);
        when(player.isShiftKeyDown()).thenReturn(true);

        InteractionResult result = block.useWithoutItem(
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                mock(BlockHitResult.class)
        );

        assertEquals(InteractionResult.SUCCESS, result);
        verify(counter).removeOldestConnected();
        verify(player).setItemInHand(InteractionHand.MAIN_HAND, oldest);
    }
    @Test
    void passesAnEmptyStackToTheEmptyHandInteraction() {
        CustomerPickupCounterBlock block = createBlock();
        Level level = mock(Level.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(counter);

        ItemInteractionResult result = block.useItemOn(
                ItemStack.EMPTY,
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                mock(Player.class),
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
                result
        );
    }

    @Test
    void keepsTheItemAndMessagesThePlayerWhenTheNetworkIsFull() {
        CustomerPickupCounterBlock block = createBlock();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack held = new ItemStack(Items.APPLE);
        ItemStack rejected = held.copy();
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(counter);
        when(counter.insertStackConnected(held)).thenReturn(rejected);

        ItemInteractionResult result = block.useItemOn(
                held,
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(ItemInteractionResult.SUCCESS, result);
        verify(player).setItemInHand(InteractionHand.MAIN_HAND, rejected);
        verify(player).displayClientMessage(
                Component.translatable(
                        "messages.customers.pickup_counter.full"
                ),
                true
        );
    }

    private static CustomerPickupCounterBlock createBlock() {
        return mock(
                CustomerPickupCounterBlock.class,
                CALLS_REAL_METHODS
        );
    }
}
