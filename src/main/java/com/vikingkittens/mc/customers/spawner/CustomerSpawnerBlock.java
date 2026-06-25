package com.vikingkittens.mc.customers.spawner;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CustomerSpawnerBlock extends BaseEntityBlock {
    public static final String NAME = "customer_spawner_block";

    private static final MapCodec<CustomerSpawnerBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, CustomerSpawnerBlock::new));

    /* package private */ static final EnumProperty<CustomerSpawnerMode> STATE_SPAWN_MODE = EnumProperty.create("spawn_mode", CustomerSpawnerMode.class);

    public CustomerSpawnerBlock(Properties properties) {
        super(properties);
        // Set the default spawn mode
        this.registerDefaultState(this.defaultBlockState().setValue(STATE_SPAWN_MODE, CustomerSpawnerMode.CONTINUOUS));
    }

    @Override
    @NotNull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE_SPAWN_MODE);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Return your custom BlockEntity here (e.g., new CustomBlockEntity(pPos, pState))
        return null;
    }

    // Required to prevent the block from being entirely invisible
    @Override
    @NotNull
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CustomerSpawnerBlockEntity entity) {
                entity.beforeRemove();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @NotNull
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            // Open up container
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CustomerSpawnerBlockEntity entity) {
                player.openMenu(entity);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    @NotNull
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Check if the player right-clicked with a clock
        if (stack.is(Items.CLOCK)) {
            if (!level.isClientSide()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof CustomerSpawnerBlockEntity entity) {
                    entity.cycleSpawnMode();
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        // TODO: Check for redstone power / pulse
        // TODO: Check for jack-o-lantern for special
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide()) {
            return (l,p,s,e) -> {
                CustomerSpawnerBlockEntity.tick(l, p, s, (CustomerSpawnerBlockEntity)e);
            };
        }
        return null;
    }
}
