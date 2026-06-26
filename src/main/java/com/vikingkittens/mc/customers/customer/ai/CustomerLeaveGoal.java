package com.vikingkittens.mc.customers.customer.ai;

import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class CustomerLeaveGoal extends MobMoveToGoal {
    private CustomerVillagerEntity customer;

    public CustomerLeaveGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, customer.getSpawnPos(), speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && customer.getState() == CustomerState.LEAVING;
    }

    @Override
    public void start() {
        Vec3 targetVec = DefaultRandomPos.getPos(customer, 32, 10);
        if (targetVec != null) {
            targetPos = BlockPos.containing(targetVec);
        }
        super.start();
    }

    @Override
    protected void onDone() {
        customer.discard();
    }
}
