package com.vikingkittens.mc.customers.customer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.PlayerCUtils;

public class CustomerPickupCounterBlock extends BaseEntityBlock {
    private static final MapCodec<CustomerPickupCounterBlock> CODEC =
            simpleCodec(CustomerPickupCounterBlock::new);
    private static final VoxelShape SHAPE =
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public CustomerPickupCounterBlock(Properties properties) {
        super(withLogStrength(properties));
    }

    static Properties withLogStrength(Properties properties) {
        return properties.strength(2.0F);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CustomerPickupCounter.BLOCK_ENTITY.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (!LevelCUtils.isClientSide(level)) {
            return (tickerLevel, pos, tickerState, entity) ->
                    CustomerPickupCounterBlockEntity.tick(
                            tickerLevel,
                            pos,
                            tickerState,
                            (CustomerPickupCounterBlockEntity) entity
                    );
        }
        return null;
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
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos)
                instanceof CustomerPickupCounterBlockEntity counter)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!LevelCUtils.isClientSide(level)) {
            ItemStack source = player.isCreative() ? stack.copy() : stack;
            ItemStack requested = source;
            if (player.isShiftKeyDown()) {
                requested = source.copy();
                requested.setCount(1);
            }
            boolean wanted =
                    counter.hasAssignableCraftedItemConnected(requested);
            ItemStack requestedRemainder =
                    counter.insertCraftedStackConnected(
                            player,
                            requested
                    );
            boolean inserted =
                    requestedRemainder.getCount() < requested.getCount();
            ItemStack remainder = requestedRemainder;
            if (player.isShiftKeyDown()) {
                remainder = source.copy();
                if (inserted) {
                    remainder.shrink(1);
                }
            }
            if (!player.isCreative()) {
                player.setItemInHand(hand, remainder);
            }
            if (!inserted) {
                PlayerCUtils.sendActionBarMessage(
                        player,
                        Component.translatable(
                                wanted
                                        ? "messages.customers.pickup_counter.full"
                                        : "messages.customers.pickup_counter.not_wanted"
                        )
                );
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof CustomerPickupCounterBlockEntity counter)) {
            return InteractionResult.PASS;
        }
        if (!LevelCUtils.isClientSide(level)) {
            ItemStack removed = counter.removeOldestConnected();
            if (!removed.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, removed);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())
                && !LevelCUtils.isClientSide(level)
                && level.getBlockEntity(pos)
                        instanceof CustomerPickupCounterBlockEntity counter) {
            for (ItemStack stack : counter.getDisplayItems()) {
                Containers.dropItemStack(
                        level,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        stack
                );
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }
}
