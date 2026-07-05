package com.vikingkittens.mc.customers.common.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class MobMoveToGoal extends MoveToBlockGoal {
    protected static final Logger LOGGER = LogManager.getLogger();

    private boolean started = false;
    private long ticksSinceStart = 0;
    private boolean doneCalled = false;

    protected BlockPos targetPos;

    public MobMoveToGoal(PathfinderMob mob, BlockPos targetPos, double speedModifier) {
        super(mob, speedModifier, 0);

        this.targetPos = targetPos;
    }

    protected long maxTicks() {
        return 20 * 20;
    }

    protected boolean isDone() {
        return isReachedTarget();
    }

    @Override
    public boolean canUse() {
        return mob.isAlive() &&
                !isDone();
    }

    @Override
    public boolean canContinueToUse() {
        boolean canContinue = super.canContinueToUse() &&
                mob.isAlive() &&
                !isDone() &&
                ticksSinceStart < maxTicks();
        if (started && !canContinue) {
            callDone();
        }
        return canContinue;
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return targetPos;
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return targetPos != null;
    }

    @Override
    public void start() {
        started = true;
        ticksSinceStart = 0;
        blockPos = targetPos;
        super.start();
    }

    @Override
    public double acceptedDistance() {
        return 1.0;
    }

    @Override
    public void tick() {
        super.tick();
        ticksSinceStart++;

        if (isDone()) {
            callDone();
        }
    }

    private void callDone() {
        if (!doneCalled) {
            doneCalled = true;
            onDone();
        }
    }

    protected void onDone() {
    }
}
