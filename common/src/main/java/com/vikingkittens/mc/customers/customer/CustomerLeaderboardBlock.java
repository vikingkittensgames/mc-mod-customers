package com.vikingkittens.mc.customers.customer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public class CustomerLeaderboardBlock extends BaseEntityBlock {
    public static final String NAME = "customer_leaderboard";
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final MapCodec<CustomerLeaderboardBlock> CODEC = simpleCodec(CustomerLeaderboardBlock::new);
    private static final VoxelShape NORTH_SHAPE = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 2.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(2.0D, 0.0D, 14.0D, 14.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 2.0D, 2.0D, 16.0D, 14.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(14.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);

    public CustomerLeaderboardBlock(Properties properties) {
        super(properties.strength(2.0F));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    static Direction resolvePlacementFacing(
            Direction clickedFace,
            Direction playerHorizontalDirection
    ) {
        if (clickedFace.getAxis().isHorizontal()) {
            return clickedFace;
        }
        return playerHorizontalDirection.getOpposite();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            net.minecraft.world.level.BlockGetter level,
            BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context
    ) {
        return getShapeForFacing(state.getValue(FACING));
    }

    static VoxelShape getShapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> SOUTH_SHAPE;
            case SOUTH -> NORTH_SHAPE;
            case WEST -> EAST_SHAPE;
            case EAST -> WEST_SHAPE;
            default -> SOUTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                resolvePlacementFacing(context.getClickedFace(), context.getHorizontalDirection())
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CustomerLeaderboard.BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        return openLeaderboard(level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        return openLeaderboard(level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean openLeaderboard(Level level, BlockPos pos, Player player) {
        if (LevelCUtils.isClientSide(level)) {
            return true;
        }
        if (player instanceof ServerPlayer serverPlayer &&
                level.getBlockEntity(pos) instanceof CustomerLeaderboardBlockEntity leaderboard) {
            leaderboard.open(serverPlayer);
            return true;
        }
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
