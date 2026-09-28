package com.vikingkittens.mc.customers.customer.pets.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

@Mixin(Mob.class)
public interface CustomerPetMobAccessor {
    @Accessor("goalSelector")
    GoalSelector customers$getGoalSelector();

    @Accessor("targetSelector")
    GoalSelector customers$getTargetSelector();
}
