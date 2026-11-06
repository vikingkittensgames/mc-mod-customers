package com.vikingkittens.mc.customers.supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import com.vikingkittens.mc.customers.common.SpawnerPresets;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public class SupplierSpawnerBlock extends BaseEntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "supplier_spawner_block";

    private static final MapCodec<SupplierSpawnerBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, SupplierSpawnerBlock::new));

    static final BooleanProperty STATE_DISABLED = BooleanProperty.create("disabled");

    public SupplierSpawnerBlock(Properties properties) {
        super(withLogStrength(properties));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(STATE_DISABLED, false)
        );
    }

    static Properties withLogStrength(Properties properties) {
        return properties.strength(2.0F);
    }

    @Override
    @NotNull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE_DISABLED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return SupplierSpawnerBlockEntity.updateState(
                context.getLevel(),
                context.getClickedPos(),
                this.defaultBlockState()
        );
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!LevelCUtils.isClientSide(level)
                && level.getBlockEntity(pos) instanceof SupplierSpawnerBlockEntity spawner) {
            applyItemSettings(stack, spawner, level.registryAccess());
        }
    }

    static void applyItemSettings(ItemStack stack, SupplierSpawnerBlockEntity spawner,
            HolderLookup.Provider registries) {
        spawner.setCustomName(SupplierSpawnerItem.getCustomName(stack).orElse(null));
        spawner.setIcon(SupplierSpawnerItem.getIcon(stack).orElse(null));
        SupplierSpawnerItem.getEnabledAppearances(stack).ifPresent(spawner::setEnabledAppearanceIds);
        SupplierSpawnerItem.getAutomaticCost(stack).ifPresent(spawner::setAutoCost);
        SupplierSpawnerItem.getItems(stack, registries).ifPresent(offers -> {
            for (int index = 0; index < Math.min(offers.size(), 24); index++) {
                int slot = index / 4 * 9 + index % 4 * 2;
                SupplierSpawnerItem.Offer offer = offers.get(index);
                spawner.getInventory().setItem(slot, offer.item().copy());
                spawner.getInventoryLocks().setLocked(slot, true);
                offer.cost().ifPresent(cost -> {
                    spawner.getInventory().setItem(slot + 1, cost.copy());
                    spawner.getInventoryLocks().setLocked(slot + 1, true);
                });
            }
        });
        spawner.setPresetId(SpawnerPresets.getId(stack).orElse(null));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
                instanceof SupplierSpawnerBlockEntity spawner) {
            drops.stream()
                    .filter(stack -> stack.is(SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get()))
                    .forEach(stack -> extractItemSettings(stack, spawner, builder.getLevel().registryAccess()));
        }
        return drops;
    }

    static void extractItemSettings(ItemStack stack, SupplierSpawnerBlockEntity spawner,
            HolderLookup.Provider registries) {
        if (spawner.getCustomName() != null) {
            SupplierSpawnerItem.setCustomName(stack, spawner.getCustomName());
        }
        if (spawner.getIcon() != null) SupplierSpawnerItem.setIcon(stack, spawner.getIcon());
        SupplierSpawnerItem.setEnabledAppearances(stack, spawner.getEnabledAppearanceIds());
        SupplierSpawnerItem.setAutomaticCost(stack, spawner.isAutoCost());
        List<SupplierSpawnerItem.Offer> offers = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int pair = 0; pair < 4; pair++) {
                int slot = row * 9 + pair * 2;
                if (!spawner.isInventoryLocked(slot) || spawner.getInventory().getItem(slot).isEmpty()) {
                    continue;
                }
                ItemStack cost = spawner.getInventory().getItem(slot + 1);
                offers.add(new SupplierSpawnerItem.Offer(
                        spawner.getInventory().getItem(slot).copy(),
                        spawner.isInventoryLocked(slot + 1) && !cost.isEmpty()
                                ? Optional.of(cost.copy()) : Optional.empty()));
            }
        }
        SupplierSpawnerItem.setItems(stack, offers, registries);
        if (spawner.getPresetId() != null) SpawnerPresets.preserve(stack, spawner.getPresetId());
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SupplierSpawnerBlockEntity(pos, state);
    }

    @Override
    @NotNull
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!LevelCUtils.isClientSide(level) && !state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SupplierSpawnerBlockEntity entity) {
                entity.beforeRemove();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @NotNull
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!LevelCUtils.isClientSide(level)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SupplierSpawnerBlockEntity entity) {
                player.openMenu(entity);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    @NotNull
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.VILLAGER_SPAWN_EGG)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SupplierSpawnerBlockEntity entity) {
                entity.spawnSupplier();
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SupplierSpawnerBlockEntity entity) {
            entity.updateState();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!LevelCUtils.isClientSide(level)) {
            return (l,p,s,e) -> {
                SupplierSpawnerBlockEntity.tick(l, p, s, (SupplierSpawnerBlockEntity)e);
            };
        }
        return null;
    }
}
