package com.vikingkittens.mc.customers.common.ai;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MobMoveToGoal extends MoveToBlockGoal {
    protected static final Logger LOGGER = LogManager.getLogger();

    private boolean started = false;
    private long ticksSinceStart = 0;
    private long maxTicks;
    private boolean doneCalled = false;
    private Path lastPath;

    protected BlockPos targetPos;
    private long ticksSinceCanReachCheck = 0;

    public MobMoveToGoal(PathfinderMob mob, BlockPos targetPos, double speedModifier) {
        super(mob, speedModifier, 0);
        setFlags(EnumSet.of(Goal.Flag.MOVE));

        this.targetPos = targetPos;
    }

    protected long maxTicks() {
        return maxTicks;
    }

    static long calculateMaxTicks(double initialDistance, double movementSpeed) {
        double estimatedBlocksPerTick = movementSpeed < 1.0
                ? movementSpeed * movementSpeed
                : movementSpeed;
        return (long)Math.ceil(initialDistance / estimatedBlocksPerTick);
    }

    static double calculatePathLength(Vec3 start, List<Vec3> nodes) {
        double distance = 0;
        Vec3 previous = start;
        for (Vec3 node : nodes) {
            distance += previous.distanceTo(node);
            previous = node;
        }
        return distance;
    }

    static long calculateDeadlineTicks(
            long elapsedTicks,
            double remainingDistance,
            double movementSpeed
    ) {
        return elapsedTicks + calculateMaxTicks(remainingDistance, movementSpeed);
    }

    protected boolean isDone() {
        return targetPos != null
                && getMoveToTarget().getBottomCenter()
                .closerThan(mob.position(), acceptedDistance());
    }

    @Override
    public boolean canUse() {
        return mob.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        boolean canContinue = super.canContinueToUse() &&
                mob.isAlive() &&
                !isDone() &&
                ticksSinceStart < maxTicks();
        if (started && !canContinue) {
            mob.getNavigation().stop();
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
        ticksSinceCanReachCheck = 0;
        lastPath = null;
        doneCalled = false;
        blockPos = targetPos;
        double initialDistance = Math.sqrt(mob.blockPosition().distSqr(targetPos));
        double movementSpeed = speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        maxTicks = calculateMaxTicks(initialDistance, movementSpeed);
        super.start();
        updateMaxTicksFromPath();
    }

    @Override
    public void stop() {
        super.stop();
        started = false;
    }

    @Override
    public double acceptedDistance() {
        return 1.5;
    }

    @Override
    public void tick() {
        if (!started) {
            return;
        }
        if (ticksSinceCanReachCheck == 0 || ticksSinceCanReachCheck > 5) {
            ticksSinceCanReachCheck = 0;
            if (mob.getNavigation().getPath() != null && !mob.getNavigation().getPath().canReach()) {
                mob.getNavigation().recomputePath();
            }
        }
        ticksSinceCanReachCheck++;

        super.tick();
        ticksSinceStart++;
        updateMaxTicksFromPath();

        if (isDone()) {
            callDone();
        }
    }

    private void updateMaxTicksFromPath() {
        Path path = mob.getNavigation().getPath();
        if (path == null || !path.canReach() || path == lastPath) {
            return;
        }
        if (lastPath != null && path.sameAs(lastPath)) {
            lastPath = path;
            return;
        }

        lastPath = path;
        List<Vec3> remainingNodes = new ArrayList<>();
        for (int index = path.getNextNodeIndex(); index < path.getNodeCount(); index++) {
            remainingNodes.add(path.getEntityPosAtNode(mob, index));
        }
        double remainingDistance = calculatePathLength(mob.position(), remainingNodes);
        if (remainingDistance > 0) {
            double movementSpeed =
                    speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            maxTicks = calculateDeadlineTicks(
                    ticksSinceStart,
                    remainingDistance,
                    movementSpeed
            );
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
