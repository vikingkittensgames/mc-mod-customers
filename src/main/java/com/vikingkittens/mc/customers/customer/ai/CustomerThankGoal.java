package com.vikingkittens.mc.customers.customer.ai;

import java.util.EnumSet;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;

import com.vikingkittens.mc.customers.common.ai.MobTimedGoal;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public class CustomerThankGoal extends MobTimedGoal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int THANK_YOU_MESSAGE_COUNT = 15;

    private final CustomerVillagerEntity customer;

    private boolean messageSent = false;
    private long ticksSinceJump = 0;
    private long ticksSinceFX = 0;
    private Component lastTradedItem = Component.empty();

    public CustomerThankGoal(CustomerVillagerEntity customer) {
        super(customer);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        customer.getOffers().stream()
                .filter(offer -> !offer.isOutOfStock())
                .findFirst()
                .ifPresent(offer ->
                        lastTradedItem = offer.getBaseCostA().getHoverName()
                );

        return super.canUse() && (
                // Happy path for state flow
                (
                        customer.getState() == CustomerState.BUYING &&
                        customer.getOffers().isEmpty()
                ) ||
                // Non-happy path where the state changed but timer never started like a server restart
                (
                        customer.getState() == CustomerState.THANKING &&
                        !started
                )
        );
    }

    @Override
    protected long maxTicks() {
        return 20 * 5;
    }

    @Override
    public void start() {
        customer.setState(CustomerState.THANKING);
        messageSent = false;
        ticksSinceJump = 0;
        ticksSinceFX = 0;
        if (customer.level().getBlockEntity(customer.getSpawnerPos()) instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.scoreboardAddCustomerServed();
        }
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
        if (!messageSent && ticksSinceStart >= 20 * 1) {
            messageSent = true;
            customer.sentPlayersMessage(
                    createThankYouMessage(
                            customer.getRandom(),
                            lastTradedItem
                    ).withColor(0x36991C)
            );
        }
        if (!customer.isPassenger() && (ticksSinceJump == 0 || ticksSinceJump > 20)) {
            customer.jumpFromGround();
            ticksSinceJump = 0;
        }
        ticksSinceJump++;
        if (ticksSinceFX == 0 || ticksSinceFX > 30) {
            customer.playLove();
            ticksSinceFX = 0;
        }
        ticksSinceFX++;
    }

    @Override
    protected void onDone() {
        customer.setState(CustomerState.DONE);
    }

    static MutableComponent createThankYouMessage(
            RandomSource random,
            Component itemName
    ) {
        int messageNumber = random.nextInt(THANK_YOU_MESSAGE_COUNT) + 1;
        return Component.translatable(
                "messages.customers.thank_you" + messageNumber,
                itemName
        );
    }
}
