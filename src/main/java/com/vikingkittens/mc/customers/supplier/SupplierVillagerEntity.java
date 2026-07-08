package com.vikingkittens.mc.customers.supplier;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.supplier.ai.SupplierMoveToSpawnGoal;
import com.vikingkittens.mc.customers.supplier.ai.SupplierMoveToSpawnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class SupplierVillagerEntity extends Villager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TAG_STATE = "SupplierState";
    private static final String TAG_SPAWNER_POS = "SpawnerPos";
    private static final String TAG_SPAWN_POS = "SpawnPos";

    public static final String NAME = "supplier_villager";

    private static VillagerType getVillagerTypeForLocation(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        BiomeVillagerType mapData = biomeHolder.getData(NeoForgeDataMaps.VILLAGER_TYPES);
        if (mapData != null) {
            return mapData.type();
        }
        return VillagerType.PLAINS;
    }

    public static SupplierVillagerEntity spawn(
            Level level,
            BlockPos spawnerPos,
            MerchantOffers offers
    ) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel)level;
            SupplierVillagerEntity supplier = Supplier.SUPPLIER_VILLAGER.get().create(level);
            if (supplier != null) {
                BlockPos safePos = MobUtils.getRandomSpawnPos(level, spawnerPos, 32, 3);
                if (safePos != null) {
                    supplier.moveTo(safePos, 0, 0);

                    LOGGER.debug("Spawning: spawner-pos={}, num-offers={}", spawnerPos, offers.size());

                    VillagerData data = supplier.getVillagerData();
                    supplier.setVillagerData(new VillagerData(
                            getVillagerTypeForLocation(level, spawnerPos),
                            Supplier.SUPPLIER_PROFESSION.get(),
                            data.getLevel()
                    ));

                    supplier.setSpawnerPos(spawnerPos);
                    supplier.setSpawnPos(safePos);
                    supplier.setOffers(offers);

                    supplier.setState(SupplierState.INITIALIZING);

                    // Finalize spawn logic (sets default items, resets AI brain, etc.)
                    supplier.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnerPos), MobSpawnType.COMMAND, null);

                    // Spawn the entity in the world
                    serverLevel.addFreshEntity(supplier);

                    return supplier;
                }
            }
        }
        return null;
    }

    private SupplierState state;
    private BlockPos spawnerPos;
    private BlockPos spawnPos;

    public SupplierVillagerEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public SupplierState getState() {
        return state;
    }

    public void setState(SupplierState state) {
        this.state = state;
    }

    public BlockPos getSpawnerPos() {
        return spawnerPos;
    }

    public void setSpawnerPos(BlockPos spawnerPos) {
        this.spawnerPos = spawnerPos;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public void setSpawnPos(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_STATE)) {
            String stateName = compound.getString(TAG_STATE);
            try {
                setState(SupplierState.valueOf(stateName));
            } catch (IllegalArgumentException exception) {
                LOGGER.warn("Ignoring unknown supplier state while loading: {}", stateName);
            }
        }
        NbtUtils.readBlockPos(compound, TAG_SPAWNER_POS).ifPresent(this::setSpawnerPos);
        NbtUtils.readBlockPos(compound, TAG_SPAWN_POS).ifPresent(this::setSpawnPos);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (state != null) {
            compound.putString(TAG_STATE, state.name());
        }
        if (spawnerPos != null) {
            compound.put(TAG_SPAWNER_POS, NbtUtils.writeBlockPos(spawnerPos));
        }
        if (spawnPos != null) {
            compound.put(TAG_SPAWN_POS, NbtUtils.writeBlockPos(spawnPos));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        // No behavior-based AI
    }

    @Override
    protected void registerGoals() {
        // Setup the goal system
        super.registerGoals();
        // Remove the standard goals
        goalSelector.removeAllGoals(goal -> true);
        // Remove the standard targets
        targetSelector.removeAllGoals(goal -> true);

        // Start with looking at the player
        goalSelector.addGoal(0, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8));

        // Customer specific goals
        goalSelector.addGoal(0, new SupplierMoveToSpawnerGoal(this, 0.5));
        goalSelector.addGoal(0, new SupplierMoveToSpawnGoal(this, 0.5));
    }

    @Override
    @NotNull
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("entity.customers.supplier_villager");
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (getState() != SupplierState.SELLING) {
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
