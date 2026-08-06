package com.vikingkittens.mc.customers.customer.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.trading.MerchantOffer;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

/**
 * Lets a buying customer collect one complete offer from its pickup counter.
 */
public class CustomerTakePickupItemGoal extends Goal {
    private static final long ATTEMPT_INTERVAL_TICKS = 20;

    private final CustomerVillagerEntity customer;
    private final CustomerMoveToCounterGoal moveGoal;
    private long lastAttemptGameTime = Long.MIN_VALUE;
    private long eligibleGameTime;

    /**
     * Creates a pickup-counter goal using the counter selected by the movement
     * goal.
     *
     * @param customer customer taking the item
     * @param moveGoal movement goal that selected the counter
     */
    public CustomerTakePickupItemGoal(
            CustomerVillagerEntity customer,
            CustomerMoveToCounterGoal moveGoal
    ) {
        this.customer = customer;
        this.moveGoal = moveGoal;
    }

    @Override
    public boolean canUse() {
        if (customer.getState() != CustomerState.BUYING
                || moveGoal.counterPosition == null
                || customer.getOffers().stream()
                        .allMatch(MerchantOffer::isOutOfStock)
                || !(customer.level().getBlockEntity(
                                moveGoal.counterPosition
                        )
                        instanceof CustomerPickupCounterBlockEntity)) {
            return false;
        }

        long gameTime = customer.level().getGameTime();
        boolean canAttempt =
                lastAttemptGameTime == Long.MIN_VALUE
                        || gameTime - lastAttemptGameTime
                                >= ATTEMPT_INTERVAL_TICKS;
        if (canAttempt) {
            eligibleGameTime = gameTime;
        }
        return canAttempt;
    }

    @Override
    public void start() {
        lastAttemptGameTime = eligibleGameTime;
        if (!(customer.level().getBlockEntity(moveGoal.counterPosition)
                instanceof CustomerPickupCounterBlockEntity counter)) {
            return;
        }

        for (MerchantOffer offer : customer.getOffers()) {
            if (offer.isOutOfStock()) {
                continue;
            }
            CustomerPickupCounterBlockEntity.StoredStack stored =
                    counter.takeMatchingStoredStack(offer);
            if (!stored.stack().isEmpty() && stored.crafterId() != null) {
                customer.completePickupCounterOffer(
                        offer,
                        stored.crafterId(),
                        moveGoal.counterPosition
                );
                return;
            }
        }
    }
}
