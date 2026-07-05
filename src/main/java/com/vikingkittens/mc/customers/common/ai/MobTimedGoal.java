package com.vikingkittens.mc.customers.common.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobTimedGoal extends Goal {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected Mob mob;
    private boolean started = false;
    protected long ticksSinceStart = 0;
    private boolean doneCalled = false;

    public MobTimedGoal(Mob mob) {
        super();
        this.mob = mob;
    }

    protected long maxTicks() {
        return 20 * 5;
    }

    protected boolean isDone() {
        return ticksSinceStart >= maxTicks();
    }


    @Override
    public boolean canUse() {
        return mob != null && maxTicks() > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !isDone();
    }

    @Override
    public void start() {
        started = true;
        ticksSinceStart = 0;
        super.start();
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
