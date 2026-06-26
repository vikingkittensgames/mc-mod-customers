package com.vikingkittens.mc.customers.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class MobMoveToGoalAndDespawn extends MobMoveToGoal {
    protected static final Logger LOGGER = LogManager.getLogger();

    public MobMoveToGoalAndDespawn(Mob mob, BlockPos targetPos, double speedModifier) {
        super(mob, targetPos, speedModifier);
    }

    @Override
    public void tick() {
        super.tick();

        if (isDone()) {
            mob.discard();
        }
    }
}
