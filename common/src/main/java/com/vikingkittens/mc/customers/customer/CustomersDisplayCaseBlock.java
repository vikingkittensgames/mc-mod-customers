package com.vikingkittens.mc.customers.customer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import com.vikingkittens.mc.customers.common.BlockUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public class CustomersDisplayCaseBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty EXTEND_LEFT =
            BooleanProperty.create("extend_left");
    public static final BooleanProperty EXTEND_RIGHT =
            BooleanProperty.create("extend_right");

    private static final MapCodec<CustomersDisplayCaseBlock> CODEC =
            simpleCodec(CustomersDisplayCaseBlock::new);

    public CustomersDisplayCaseBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(EXTEND_LEFT, false)
                        .setValue(EXTEND_RIGHT, false)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(
                FACING,
                BlockUtils.resolveHorizontalPlacementFacing(
                        context.getClickedFace(),
                        context.getHorizontalDirection()
                )
        );
        return updateConnections(
                state,
                context.getLevel(),
                context.getClickedPos()
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CustomersDisplayCase.BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (player.isSecondaryUseActive()) {
            return ItemInteractionResult
                    .PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return openContainer(level, pos, player)
                ? ItemInteractionResult.SUCCESS
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        return openContainer(level, pos, player)
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    private static boolean openContainer(
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (LevelCUtils.isClientSide(level)) {
            return true;
        }
        if (level.getBlockEntity(pos)
                instanceof CustomersDisplayCaseBlockEntity displayCase) {
            player.openMenu(displayCase);
            return true;
        }
        return false;
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(
                level.getBlockEntity(pos)
        );
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return updateConnections(state, level, pos);
    }

    static BlockState updateConnections(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        Direction facing = state.getValue(FACING);
        return state
                .setValue(
                        EXTEND_LEFT,
                        matchesNeighbor(
                                state,
                                level.getBlockState(pos.relative(
                                        getLeftDirection(facing)
                                ))
                        )
                )
                .setValue(
                        EXTEND_RIGHT,
                        matchesNeighbor(
                                state,
                                level.getBlockState(pos.relative(
                                        getRightDirection(facing)
                                ))
                        )
                );
    }

    static boolean matchesNeighbor(
            BlockState state,
            BlockState neighbor
    ) {
        return neighbor.getBlock() == state.getBlock()
                && neighbor.hasProperty(FACING)
                && neighbor.getValue(FACING) == state.getValue(FACING);
    }

    static Direction getLeftDirection(Direction facing) {
        return facing.getClockWise();
    }

    static Direction getRightDirection(Direction facing) {
        return facing.getCounterClockWise();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state.rotate(
                mirror.getRotation(state.getValue(FACING))
        );
        if (!shouldSwapExtensions(mirror)) {
            return mirrored;
        }
        return mirrored
                .setValue(
                        EXTEND_LEFT,
                        state.getValue(EXTEND_RIGHT)
                )
                .setValue(
                        EXTEND_RIGHT,
                        state.getValue(EXTEND_LEFT)
                );
    }

    static boolean shouldSwapExtensions(Mirror mirror) {
        return mirror != Mirror.NONE;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, EXTEND_LEFT, EXTEND_RIGHT);
    }

}
