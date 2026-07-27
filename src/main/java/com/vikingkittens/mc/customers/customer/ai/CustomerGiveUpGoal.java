package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobTimedGoal;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.slf4j.Logger;

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
        long giveUpTicks = 20L * Config.CUSTOMER_GIVE_UP_SECONDS.get();
        VillagerProfession profession = customer.getVillagerData().getProfession();
        if (profession == Customer.CUSTOMER_IMPATIENT_PROFESSION.get()) {
            giveUpTicks = Math.max(1, giveUpTicks / 2);
        } else if (profession == Customer.CUSTOMER_CASUAL_PROFESSION.get()) {
            giveUpTicks = 0;
        }
        return super.canUse() && (
                // Happy path for state flow
                (
                        (
                                customer.getState() == CustomerState.BUYING &&
                                (giveUpTicks > 0 && customer.getTicksSinceTrade() > giveUpTicks)
                        ) ||
                                customer.getState() == CustomerState.FORCED_GIVING_UP
                ) ||
                        // Non-happy path where the state changed but timer never started like a server restart
                        (
                                customer.getState() == CustomerState.GIVING_UP && !started
                        )
        );
    }

    @Override
    protected long maxTicks() {
        return 20 * 3;
    }

    @Override
    public void start() {
        // LOGGER.debug("Giving up");
        if (customer.getState() == CustomerState.FORCED_GIVING_UP) {
            messageSent = true;
        }
        customer.setState(CustomerState.GIVING_UP);
        ticksSinceFX = 0;
        if (customer.level().getBlockEntity(customer.getSpawnerPos()) instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.scoreboardAddCustomerGaveUp();
        }
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
        if (!messageSent && ticksSinceStart >= 20 * 1) {
            messageSent = true;
            customer.sentPlayersMessage(Component.translatable("messages.customers.give_up").withColor(0xFF0000));
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
