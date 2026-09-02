package com.vikingkittens.mc.customers.customer.pets.ai;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public final class CustomerPetFollowCustomerGoal extends Goal {
    private static final double FOLLOW_SPEED = 1.0D;
    private static final float FOLLOW_START_DISTANCE = 2.0F;
    private static final float FOLLOW_STOP_DISTANCE = 1.0F;
    private static final double TELEPORT_DISTANCE_SQR = 144.0D;

    private final Animal pet;
    private final UUID customerId;
    private CustomerVillagerEntity customer;
    private int timeToRecalculatePath;

    public CustomerPetFollowCustomerGoal(Animal pet, UUID customerId) {
        this.pet = pet;
        this.customerId = customerId;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return hasLivingCustomerOrDiscardPet() &&
                pet.distanceToSqr(customer) >= FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        return hasLivingCustomerOrDiscardPet() &&
                pet.distanceToSqr(customer) > FOLLOW_STOP_DISTANCE * FOLLOW_STOP_DISTANCE;
    }

    @Override
    public void start() {
        if (pet instanceof TamableAnimal tamablePet && tamablePet.isOrderedToSit()) {
            tamablePet.setOrderedToSit(false);
        }
    }

    @Override
    public void stop() {
        customer = null;
        pet.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!hasLivingCustomerOrDiscardPet()) {
            return;
        }
        pet.getLookControl().setLookAt(customer, 10.0F, pet.getMaxHeadXRot());
        if (pet.distanceToSqr(customer) >= TELEPORT_DISTANCE_SQR) {
            EntityCUtils.snapTo(pet, customer.position(), customer.getYRot(), customer.getXRot());
            pet.getNavigation().stop();
        } else if (--timeToRecalculatePath <= 0) {
            timeToRecalculatePath = adjustedTickDelay(10);
            pet.getNavigation().moveTo(customer, FOLLOW_SPEED);
        }
    }

    private boolean hasLivingCustomerOrDiscardPet() {
        customer = findCustomer();
        if (customer != null) {
            return true;
        }
        pet.discard();
        return false;
    }

    private CustomerVillagerEntity findCustomer() {
        if (pet.level() instanceof ServerLevel serverLevel &&
                serverLevel.getEntity(customerId) instanceof CustomerVillagerEntity customer &&
                customer.isAlive()) {
            return customer;
        }
        return null;
    }
}
