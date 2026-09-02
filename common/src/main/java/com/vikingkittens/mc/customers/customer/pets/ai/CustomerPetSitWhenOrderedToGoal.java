package com.vikingkittens.mc.customers.customer.pets.ai;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;

import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public final class CustomerPetSitWhenOrderedToGoal extends SitWhenOrderedToGoal {
    private final TamableAnimal pet;
    private final UUID customerId;

    public CustomerPetSitWhenOrderedToGoal(TamableAnimal pet, UUID customerId) {
        super(pet);
        this.pet = pet;
        this.customerId = customerId;
    }

    @Override
    public boolean canUse() {
        return isCustomerBuying() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return isCustomerBuying() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        super.stop();
        pet.setOrderedToSit(false);
    }

    private boolean isCustomerBuying() {
        return pet.level() instanceof ServerLevel serverLevel &&
                serverLevel.getEntity(customerId) instanceof CustomerVillagerEntity customer &&
                customer.isAlive() &&
                customer.getState() == CustomerState.BUYING;
    }
}
