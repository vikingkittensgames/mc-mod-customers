package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class CustomerLeaveGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomerVillagerEntity customer;

    public CustomerLeaveGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, customer.getSpawnPos(), speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                customer.getState() == CustomerState.LEAVING;
    }

    @Override
    public void start() {
        Vec3 targetVec = DefaultRandomPos.getPos(customer, 32, 10);
        if (targetVec != null) {
            targetPos = BlockPos.containing(targetVec);
        } else {
            targetPos = mob.blockPosition();
        }
        LOGGER.debug("Target positions: {}", targetPos);
        customer.setState(CustomerState.MOVING_TO_DESPAWN);
        super.start();
    }

    @Override
    public double acceptedDistance() {
        return 2;
    }

    @Override
    protected void onDone() {
        LOGGER.debug("Reached despawn");
        customer.discard();
    }
}
