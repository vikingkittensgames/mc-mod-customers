package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CustomersDisplayCaseBlockTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void connectsOnlyToTheExactVariationWithTheSameFacing() {
        Block block = mock(Block.class);
        Block otherVariation = mock(Block.class);
        BlockState state = state(block, Direction.NORTH);

        assertTrue(CustomersDisplayCaseBlock.matchesNeighbor(
                state,
                state
        ));
        assertFalse(CustomersDisplayCaseBlock.matchesNeighbor(
                state,
                state(otherVariation, Direction.NORTH)
        ));
        assertFalse(CustomersDisplayCaseBlock.matchesNeighbor(
                state,
                state(block, Direction.SOUTH)
        ));
        assertFalse(CustomersDisplayCaseBlock.matchesNeighbor(
                state,
                Blocks.AIR.defaultBlockState()
        ));
    }

    @Test
    void resolvesLeftAndRightRelativeToTheCaseFront() {
        assertEquals(
                Direction.EAST,
                CustomersDisplayCaseBlock.getLeftDirection(
                        Direction.NORTH
                )
        );
        assertEquals(
                Direction.WEST,
                CustomersDisplayCaseBlock.getRightDirection(
                        Direction.NORTH
                )
        );
        assertEquals(
                Direction.WEST,
                CustomersDisplayCaseBlock.getLeftDirection(
                        Direction.SOUTH
                )
        );
        assertEquals(
                Direction.EAST,
                CustomersDisplayCaseBlock.getRightDirection(
                        Direction.SOUTH
                )
        );
    }

    @Test
    void updatesExtensionFlagsFromMatchingNeighbors() {
        Block block = mock(Block.class);
        BlockState state = state(block, Direction.NORTH);
        BlockState withLeft = state(block, Direction.NORTH);
        BlockState connected = state(block, Direction.NORTH);
        BlockGetter level = mock(BlockGetter.class);
        BlockPos pos = BlockPos.ZERO;
        when(level.getBlockState(pos.east())).thenReturn(state);
        when(level.getBlockState(pos.west()))
                .thenReturn(Blocks.AIR.defaultBlockState());
        when(state.setValue(
                CustomersDisplayCaseBlock.EXTEND_LEFT,
                true
        )).thenReturn(withLeft);
        when(withLeft.setValue(
                CustomersDisplayCaseBlock.EXTEND_RIGHT,
                false
        )).thenReturn(connected);
        when(connected.getValue(
                CustomersDisplayCaseBlock.EXTEND_LEFT
        )).thenReturn(true);
        when(connected.getValue(
                CustomersDisplayCaseBlock.EXTEND_RIGHT
        )).thenReturn(false);

        BlockState result =
                CustomersDisplayCaseBlock.updateConnections(
                        state,
                        level,
                        pos
                );

        assertTrue(result.getValue(
                CustomersDisplayCaseBlock.EXTEND_LEFT
        ));
        assertFalse(result.getValue(
                CustomersDisplayCaseBlock.EXTEND_RIGHT
        ));
    }

    @Test
    void exposesNamedFacingAndExtensionProperties() {
        assertEquals("facing", CustomersDisplayCaseBlock.FACING.getName());
        assertEquals(
                "extend_left",
                CustomersDisplayCaseBlock.EXTEND_LEFT.getName()
        );
        assertEquals(
                "extend_right",
                CustomersDisplayCaseBlock.EXTEND_RIGHT.getName()
        );
    }

    @Test
    void swapsExtensionSidesOnlyForAnActualMirror() {
        assertFalse(CustomersDisplayCaseBlock.shouldSwapExtensions(Mirror.NONE));
        assertTrue(CustomersDisplayCaseBlock.shouldSwapExtensions(Mirror.LEFT_RIGHT));
        assertTrue(CustomersDisplayCaseBlock.shouldSwapExtensions(Mirror.FRONT_BACK));
    }

    @Test
    void crouchUseWithAnItemPassesThroughForBlockPlacement() {
        CustomersDisplayCaseBlock block = mock(
                CustomersDisplayCaseBlock.class,
                CALLS_REAL_METHODS
        );
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        when(player.isSecondaryUseActive()).thenReturn(true);

        ItemInteractionResult result = block.useItemOn(
                new ItemStack(Items.STONE),
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                InteractionHand.MAIN_HAND,
                mock(BlockHitResult.class)
        );

        assertEquals(
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
                result
        );
        verifyNoInteractions(level);
    }

    @Test
    void crouchUseWithAnEmptyHandDoesNotOpenTheMenu() {
        CustomersDisplayCaseBlock block = mock(
                CustomersDisplayCaseBlock.class,
                CALLS_REAL_METHODS
        );
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        when(player.isSecondaryUseActive()).thenReturn(true);

        InteractionResult result = block.useWithoutItem(
                mock(BlockState.class),
                level,
                BlockPos.ZERO,
                player,
                mock(BlockHitResult.class)
        );

        assertEquals(InteractionResult.PASS, result);
        verifyNoInteractions(level);
    }

    private static BlockState state(
            Block block,
            Direction facing
    ) {
        BlockState state = mock(BlockState.class);
        when(state.getBlock()).thenReturn(block);
        when(state.hasProperty(CustomersDisplayCaseBlock.FACING))
                .thenReturn(true);
        when(state.getValue(CustomersDisplayCaseBlock.FACING))
                .thenReturn(facing);
        return state;
    }
}
