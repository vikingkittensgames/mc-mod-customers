package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import com.vikingkittens.mc.customers.common.SpawnerPresets;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.customer.pets.CustomerPet;

public class CustomerSpawnerBlock extends BaseEntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "customer_spawner_block";

    private static final MapCodec<CustomerSpawnerBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, CustomerSpawnerBlock::new));

    public static final EnumProperty<CustomerSpawnerMode> STATE_SPAWN_MODE = EnumProperty.create("spawn_mode", CustomerSpawnerMode.class);

    static final BooleanProperty STATE_DISABLED = BooleanProperty.create("disabled");

    static final BooleanProperty STATE_POWERED = BooleanProperty.create("powered");

    public CustomerSpawnerBlock(Properties properties) {
        super(withLogStrength(properties));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(STATE_SPAWN_MODE, CustomerSpawnerMode.CONTINUOUS)
                .setValue(STATE_DISABLED, false)
                .setValue(STATE_POWERED, false)
        );
    }

    static Properties withLogStrength(Properties properties) {
        return properties.strength(2.0F);
    }

    public static BlockState getMarkerState(CustomerSpawnerMode spawnerMode) {
        return CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.get()
                .defaultBlockState()
                .setValue(STATE_SPAWN_MODE, spawnerMode)
                .setValue(STATE_DISABLED, false);
    }

    @Override
    @NotNull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE_SPAWN_MODE);
        builder.add(STATE_DISABLED);
        builder.add(STATE_POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        CustomerSpawnerMode mode = CustomerSpawnerItem.getMode(context.getItemInHand())
                .orElse(CustomerSpawnerMode.CONTINUOUS);
        return CustomerSpawnerBlockEntity.updateState(
                context.getLevel(),
                context.getClickedPos(),
                this.defaultBlockState().setValue(STATE_SPAWN_MODE, mode)
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
                && level.getBlockEntity(pos) instanceof CustomerSpawnerBlockEntity spawner) {
            applyItemSettings(stack, spawner, level.registryAccess());
        }
    }

    static void applyItemSettings(ItemStack stack, CustomerSpawnerBlockEntity spawner,
            HolderLookup.Provider registries) {
        spawner.setCustomName(CustomerSpawnerItem.getCustomName(stack).orElse(null));
        spawner.setIcon(CustomerSpawnerItem.getIcon(stack).orElse(null));
        int levelCount = CustomerSpawnerItem.getLevelCount(stack);
        if (levelCount > 0) {
            spawner.resetLevelSettings(levelCount);
            for (int index = 0; index < spawner.getLevelSettingsCount(); index++) {
                CustomerSpawnerLevelSettings settings = spawner.getLevelSettings(index);
                CustomerSpawnerItem.getRequiredStars(stack, index).ifPresent(settings::setRequiredStars);
                CustomerSpawnerItem.getMaxCustomers(stack, index).ifPresent(settings::setMaxCustomers);
                CustomerSpawnerItem.getPetPercentage(stack, index).ifPresent(settings::setPetPercentage);
                CustomerSpawnerItem.getEnabledPets(stack, index).ifPresent(pets -> {
                    settings.setPetTypeSettings(true, pets.stream().map(CustomerSpawnerItem.EnabledPet::id).toList());
                    Map<String, ItemStack> selected = new java.util.LinkedHashMap<>();
                    pets.forEach(pet -> pet.food().ifPresent(foodId -> {
                        net.minecraft.world.item.Item food = BuiltInRegistries.ITEM.get(foodId);
                        if (food != Items.AIR) selected.put(pet.id(), new ItemStack(food));
                    }));
                    settings.setConfiguredPetFoods(selected);
                });
                CustomerSpawnerItem.getEnabledAppearances(stack, index).ifPresent(settings::setEnabledAppearanceIds);
                CustomerSpawnerItem.getAutomaticCost(stack, index).ifPresent(settings::setAutoCost);
                CustomerSpawnerItem.getRows(stack, index, registries).ifPresent(rows -> {
                    for (int row = 0; row < Math.min(rows.size(), 6); row++) {
                        CustomerSpawnerItem.Row presetRow = rows.get(row);
                        for (int item = 0; item < Math.min(presetRow.items().size(), 8); item++) {
                            int slot = row * 9 + item;
                            settings.getInventory().setItem(slot, presetRow.items().get(item).copy());
                            settings.getInventoryLocks().setLocked(slot, true);
                        }
                        int costSlot = row * 9 + 8;
                        presetRow.cost().ifPresent(cost -> {
                            settings.getInventory().setItem(costSlot, cost.copy());
                            settings.getInventoryLocks().setLocked(costSlot, true);
                        });
                    }
                });
            }
        }
        spawner.setPresetId(SpawnerPresets.getId(stack).orElse(null));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
                instanceof CustomerSpawnerBlockEntity spawner) {
            CustomerSpawnerMode mode = state.getValue(STATE_SPAWN_MODE);
            drops.stream()
                    .filter(stack -> stack.is(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get()))
                    .forEach(stack -> extractItemSettings(stack, mode, spawner, builder.getLevel().registryAccess()));
        }
        return drops;
    }

    static void extractItemSettings(ItemStack stack, CustomerSpawnerMode mode, CustomerSpawnerBlockEntity spawner,
            HolderLookup.Provider registries) {
        if (spawner.getCustomName() != null) {
            CustomerSpawnerItem.setCustomName(stack, spawner.getCustomName());
        }
        if (spawner.getIcon() != null) CustomerSpawnerItem.setIcon(stack, spawner.getIcon());
        CustomerSpawnerItem.setMode(stack, mode);
        CustomerSpawnerItem.setLevelCount(stack, spawner.getLevelSettingsCount());
            Map<String, ItemStack> defaultPetFoods = new java.util.HashMap<>();
            if (spawner.getLevel() != null) {
                CustomerPet.getAvailablePetTypes(spawner.getLevel()).forEach(pet ->
                        defaultPetFoods.put(pet.entityId(), pet.foods().getFirst()));
            }
        for (int index = 0; index < spawner.getLevelSettingsCount(); index++) {
            CustomerSpawnerLevelSettings settings = spawner.getLevelSettings(index);
            CustomerSpawnerItem.setRequiredStars(stack, index, settings.getRequiredStars());
            CustomerSpawnerItem.setMaxCustomers(stack, index, settings.getMaxCustomers());
            CustomerSpawnerItem.setPetPercentage(stack, index, settings.getPetPercentage());
                Map<String, ItemStack> selectedFoods = settings.getConfiguredPetFoods();
                List<String> petIds = settings.isPetTypesCustomized()
                        ? settings.getConfiguredPetTypeIds() : selectedFoods.keySet().stream().sorted().toList();
                if (settings.isPetTypesCustomized() || !petIds.isEmpty()) {
                    List<CustomerSpawnerItem.EnabledPet> pets = new ArrayList<>();
                    petIds.forEach(id -> {
                        ItemStack food = selectedFoods.get(id);
                        pets.add(new CustomerSpawnerItem.EnabledPet(id,
                            food == null || food.isEmpty() || (defaultPetFoods.containsKey(id) &&
                                    ItemStackCUtils.isSameItemAndTags(food, defaultPetFoods.get(id))) ? Optional.empty()
                                        : Optional.of(BuiltInRegistries.ITEM.getKey(food.getItem()))));
                    });
                    CustomerSpawnerItem.setEnabledPets(stack, index, pets);
                }
            CustomerSpawnerItem.setEnabledAppearances(stack, index, settings.getEnabledAppearanceIds());
            CustomerSpawnerItem.setAutomaticCost(stack, index, settings.isAutoCost());
            List<CustomerSpawnerItem.Row> rows = new ArrayList<>();
            for (int row = 0; row < 6; row++) {
                List<ItemStack> items = new ArrayList<>();
                for (int item = 0; item < 8; item++) {
                    int slot = row * 9 + item;
                    if (settings.isInventoryLocked(slot) && !settings.getInventory().getItem(slot).isEmpty()) {
                        items.add(settings.getInventory().getItem(slot).copy());
                    }
                }
                int costSlot = row * 9 + 8;
                ItemStack cost = settings.getInventory().getItem(costSlot);
                rows.add(new CustomerSpawnerItem.Row(items,
                        settings.isInventoryLocked(costSlot) && !cost.isEmpty()
                                ? Optional.of(cost.copy()) : Optional.empty()));
            }
            while (!rows.isEmpty() && rows.getLast().items().isEmpty() && rows.getLast().cost().isEmpty()) {
                rows.removeLast();
            }
            CustomerSpawnerItem.setRows(stack, index, rows, registries);
        }
        if (spawner.getPresetId() != null) SpawnerPresets.preserve(stack, spawner.getPresetId());
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomerSpawnerBlockEntity(pos, state);
    }

    @Override
    @NotNull
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!LevelCUtils.isClientSide(level) && !state.is(newState.getBlock())) {
            CustomerSpawnerCache.remove(level, pos);
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
        if (!LevelCUtils.isClientSide(level)) {
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
        if (stack.is(Items.CLOCK)) {
            if (!LevelCUtils.isClientSide(level)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof CustomerSpawnerBlockEntity entity) {
                    entity.cycleSpawnMode();
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.VILLAGER_SPAWN_EGG)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CustomerSpawnerBlockEntity entity) {
                entity.spawnCustomer();
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CustomerSpawnerBlockEntity entity) {
            if (!LevelCUtils.isClientSide(level)) {
                CustomerSpawnerCache.update(level, pos);
            }
            entity.updateState();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!LevelCUtils.isClientSide(level)) {
            return (l,p,s,e) -> {
                CustomerSpawnerBlockEntity.tick(l, p, s, (CustomerSpawnerBlockEntity)e);
            };
        }
        return null;
    }
}
