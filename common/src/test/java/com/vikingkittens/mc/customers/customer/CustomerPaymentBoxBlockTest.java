package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPaymentBoxBlockTest {
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
                CustomerPaymentBoxBlock.withLogStrength(properties);

        assertSame(properties, result);
        verify(properties).strength(2.0F);
    }

    @Test
    void occupiesFourteenPixelsWithHorizontalInsets() {
        CustomerPaymentBoxBlock block =
                createBlock();
        VoxelShape shape = block.getShape(
                mock(BlockState.class),
                mock(BlockGetter.class),
                BlockPos.ZERO,
                mock(CollisionContext.class)
        );

        assertEquals(1.0D / 16.0D, shape.bounds().minX);
        assertEquals(0.0D, shape.bounds().minY);
        assertEquals(1.0D / 16.0D, shape.bounds().minZ);
        assertEquals(15.0D / 16.0D, shape.bounds().maxX);
        assertEquals(14.0D / 16.0D, shape.bounds().maxY);
        assertEquals(15.0D / 16.0D, shape.bounds().maxZ);
    }

    @Test
    void facesAwayFromTargetedHorizontalSide() {
        assertEquals(
                Direction.EAST,
                CustomerPaymentBoxBlock.resolvePlacementFacing(
                        Direction.EAST,
                        Direction.NORTH
                )
        );
    }

    @Test
    void facesPlayerWhenPlacedOnFloorOrCeiling() {
        assertEquals(
                Direction.SOUTH,
                CustomerPaymentBoxBlock.resolvePlacementFacing(
                        Direction.UP,
                        Direction.NORTH
                )
        );
        assertEquals(
                Direction.EAST,
                CustomerPaymentBoxBlock.resolvePlacementFacing(
                        Direction.DOWN,
                        Direction.WEST
                )
        );
    }

    @Test
    void opensContainerWhenHoldingAnItem() {
        CustomerPaymentBoxBlock block = createBlock();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerPaymentBoxBlockEntity paymentBox =
                mock(CustomerPaymentBoxBlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(paymentBox);

        InteractionResult result = block.useItemOn(
                new ItemStack(Items.STICK),
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(InteractionResult.SUCCESS, result);
        verify(player).openMenu(paymentBox);
    }

    @Test
    void opensContainerWithEmptyHand() {
        CustomerPaymentBoxBlock block = createBlock();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        CustomerPaymentBoxBlockEntity paymentBox =
                mock(CustomerPaymentBoxBlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(paymentBox);

        InteractionResult result = block.useWithoutItem(
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                mock(BlockHitResult.class)
        );

        assertEquals(InteractionResult.CONSUME, result);
        verify(player).openMenu(paymentBox);
    }

    @Test
    void dropsContainerContentsWhenBroken() {
        CustomerPaymentBoxBlock block = createBlock();
        BlockState state = mock(BlockState.class);
        BlockState replacement = mock(BlockState.class);
        Level level = mock(Level.class);

        block.onRemove(
                state,
                level,
                BlockPos.ZERO,
                replacement,
                false
        );
    }

    private static CustomerPaymentBoxBlock createBlock() {
        return mock(
                CustomerPaymentBoxBlock.class,
                CALLS_REAL_METHODS
        );
    }
}
