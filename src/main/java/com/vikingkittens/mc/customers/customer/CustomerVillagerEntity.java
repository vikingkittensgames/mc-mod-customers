package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class CustomerVillagerEntity extends Villager {
    private static final Logger LOGGER = LogUtils.getLogger();

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
}
