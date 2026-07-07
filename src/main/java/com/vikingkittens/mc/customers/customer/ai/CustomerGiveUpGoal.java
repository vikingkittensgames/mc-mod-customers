package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobTimedGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.List;

public class CustomerGiveUpGoal extends MobTimedGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomerVillagerEntity customer;

    private boolean messageSent = false;
    private long ticksSinceFX = 0;

    public CustomerGiveUpGoal(CustomerVillagerEntity customer) {
        super(customer);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && customer.getState() == CustomerState.BUYING && customer.getTicksSinceTrade() > (20 * 60);
    }

    @Override
    protected long maxTicks() {
        return 20 * 3;
    }

    @Override
    public void start() {
        // LOGGER.debug("Giving up");
        customer.setState(CustomerState.GIVING_UP);
        ticksSinceFX = 0;
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
        if (!messageSent && ticksSinceStart >= 20 * 1) {
            messageSent = true;
            List<Player> players = customer.level().getNearbyPlayers(
                    TargetingConditions.forNonCombat().ignoreLineOfSight(),
                    customer,
                    AABB.ofSize(customer.position(), 32, 32, 32)
            );
            for (Player player : players) {
                try {
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("messages.customers.give_up").withColor(0xFF0000), true);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to sent message to player", t);
                }
            }
        }
        if (ticksSinceFX == 0 || ticksSinceFX > 30) {
            customer.playAngry();
            ticksSinceFX = 0;
        }
        ticksSinceFX++;
    }

    @Override
    protected void onDone() {
        // LOGGER.debug("Done giving up");
        customer.setState(CustomerState.DONE);
    }
}
