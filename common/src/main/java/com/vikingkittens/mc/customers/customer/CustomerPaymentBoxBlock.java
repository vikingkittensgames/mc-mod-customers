package com.vikingkittens.mc.customers.customer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public class CustomerPaymentBoxBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;

    public CustomerPaymentBoxBlock(Properties properties) {
        super(withLogStrength(properties));
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    static Properties withLogStrength(Properties properties) {
        return properties.strength(2.0F);
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                resolvePlacementFacing(
                        context.getClickedFace(),
                        context.getHorizontalDirection()
                )
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CustomerPaymentBox.BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        return openContainer(level, pos, player);
    }

    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        openContainer(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        return openContainer(level, pos, player);
    }

    private static InteractionResult openContainer(
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (LevelCUtils.isClientSide(level)) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos)
                instanceof CustomerPaymentBoxBlockEntity paymentBox) {
            player.openMenu(paymentBox);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos)
                instanceof CustomerPaymentBoxBlockEntity paymentBox) {
            for (int slot = 0; slot < paymentBox.getContainerSize(); slot++) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D, paymentBox.removeItemNoUpdate(slot));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(
                level.getBlockEntity(pos)
        );
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(
                mirror.getRotation(state.getValue(FACING))
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }
}
