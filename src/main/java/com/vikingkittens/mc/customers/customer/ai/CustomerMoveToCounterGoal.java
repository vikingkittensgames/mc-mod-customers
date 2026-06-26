package com.vikingkittens.mc.customers.customer.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

public class CustomerMoveToCounterGoal extends MoveToBlockGoal {
    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos pos) {
        // TODO: Check to see if pos is next to and at the same y or below by up to 2
        return false;
    }
}
