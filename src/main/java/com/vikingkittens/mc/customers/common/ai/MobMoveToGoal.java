package com.vikingkittens.mc.customers.common.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class MobMoveToGoal extends Goal {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected static final double TARGET_DISTANCE_THRESHOLD = 1.5;

    protected Mob mob;
    protected BlockPos targetPos;
    protected double speedModifier;

    public MobMoveToGoal(Mob mob, BlockPos targetPos, double speedModifier) {
        this.mob = mob;
        this.targetPos = targetPos;
        this.speedModifier = speedModifier;

        // Flag.MOVE prevents conflicting pathfinding behaviors (like wandering or panicking)
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected boolean isDone() {
        return targetPos != null && mob.blockPosition().closerThan(targetPos, TARGET_DISTANCE_THRESHOLD);
    }

    @Override
    public boolean canUse() {
        return mob.isAlive() &&
                !mob.getNavigation().isInProgress() &&
                !isDone();
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isAlive() &&
                !isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(
                targetPos.getX(),
                targetPos.getY(),
                targetPos.getZ(),
                speedModifier
        );
    }

    @Override
    public void tick() {
        // If the pathfinder loses its path mid-walk, re-apply the path command
        if (mob.getNavigation().isDone() && !isDone()) {
            start();
        }

        if (isDone()) {
            onDone();
        }
    }

    protected void onDone() {
    }
}
