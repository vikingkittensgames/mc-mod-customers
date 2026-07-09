package com.vikingkittens.mc.customers.customer.special;

import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CustomerZombieEntity extends CustomerVillagerEntity {
    public static final String NAME = "customer_zombie";

    public CustomerZombieEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    // Plays when the entity is idle
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    // Plays when the entity takes damage
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    // Plays when the entity dies
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    // Plays the physical thudding step sound when walking
    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }
}

