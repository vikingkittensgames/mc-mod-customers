package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobTimedGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.UUID;

public class CustomerThankGoal extends MobTimedGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomerVillagerEntity customer;

    private boolean messageSent = false;
    private long ticksSinceJump = 0;
    private long ticksSinceFX = 0;

    public CustomerThankGoal(CustomerVillagerEntity customer) {
        super(customer);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && customer.getState() == CustomerState.BUYING && customer.getOffers().isEmpty();
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
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
        if (!messageSent && ticksSinceStart >= 20 * 1) {
            messageSent = true;
            for (UUID playerUUID : customer.getTradedWithPlayers()) {
                try {
                    Player player = customer.level().getPlayerByUUID(playerUUID);
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("messages.customers.thank_you", player.getDisplayName()).withColor(0x36991C), true);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to sent message to player", t);
                }
            }
        }
        if (ticksSinceJump == 0 || ticksSinceJump > 20) {
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
}
