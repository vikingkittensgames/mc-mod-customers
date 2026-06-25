package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.slf4j.Logger;

public class CustomerVillagerEntity extends Villager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "customer_villager";

    private static VillagerType getVillagerTypeForLocation(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        BiomeVillagerType mapData = biomeHolder.getData(NeoForgeDataMaps.VILLAGER_TYPES);
        if (mapData != null) {
            return mapData.type();
        }
        return VillagerType.PLAINS;
    }

    public static void spawn(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel)level;
            CustomerVillagerEntity customer = Customer.CUSTOMER_VILLAGER.get().create(level);
            if (customer != null) {
                customer.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

                VillagerData data = customer.getVillagerData();
                customer.setVillagerData(new VillagerData(
                        getVillagerTypeForLocation(level, pos),
                        Customer.CUSTOMER_PROFESSION.get(),
                        data.getLevel()
                ));

                customer.setSpawnPos(pos);

                // Finalize spawn logic (sets default items, resets AI brain, etc.)
                customer.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);

                // Spawn the entity in the world
                serverLevel.addFreshEntity(customer);
            }
        }
    }

    private BlockPos spawnPos;

    public CustomerVillagerEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public void setSpawnPos(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
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
        this.goalSelector.removeAllGoals(goal -> true);
        // Remove the standard targets
        this.targetSelector.removeAllGoals(goal -> true);

        // TODO: Custom goals to find counter/table-top
    }
}
