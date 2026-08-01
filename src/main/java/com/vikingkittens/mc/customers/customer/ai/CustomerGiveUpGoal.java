package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobTimedGoal;
import com.vikingkittens.mc.customers.compatability.VillagerCUtils;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.trading.MerchantOffer;
import org.slf4j.Logger;

import java.util.List;

public class CustomerGiveUpGoal extends MobTimedGoal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GIVE_UP_MESSAGE_COUNT = 15;

    private final CustomerVillagerEntity customer;

    private boolean messageSent = false;
    private long ticksSinceFX = 0;

    public CustomerGiveUpGoal(CustomerVillagerEntity customer) {
        super(customer);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        long giveUpTicks = 20L * Config.CUSTOMER_GIVE_UP_SECONDS.get();
        if (VillagerCUtils.hasProfession(
                customer.getVillagerData(),
                Customer.CUSTOMER_IMPATIENT_PROFESSION.getKey()
        )) {
            giveUpTicks = Math.max(1, giveUpTicks / 2);
        } else if (VillagerCUtils.hasProfession(
                customer.getVillagerData(),
                Customer.CUSTOMER_CASUAL_PROFESSION.getKey()
        )) {
            giveUpTicks = 0;
        }
        return super.canUse() && (
                (
                        (
                                customer.getState() == CustomerState.BUYING &&
                                (giveUpTicks > 0 && customer.getTicksSinceTrade() > giveUpTicks)
                        ) ||
                                customer.getState() == CustomerState.FORCED_GIVING_UP
                ) ||
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
            customer.sentPlayersMessage(
                    createGiveUpMessage(
                            customer.getRandom(),
                            customer.getOffers()
                    ).withColor(0xFF0000)
            );
        }
        if (ticksSinceFX == 0 || ticksSinceFX > 30) {
            customer.playAngry();
            ticksSinceFX = 0;
        }
        ticksSinceFX++;
    }

    @Override
    protected void onDone() {
        customer.setState(CustomerState.DONE);
    }

    static MutableComponent createGiveUpMessage(
            RandomSource random,
            List<MerchantOffer> offers
    ) {
        int messageNumber = random.nextInt(GIVE_UP_MESSAGE_COUNT) + 1;
        List<Component> remainingItemNames = offers.stream()
                .filter(offer -> !offer.isOutOfStock())
                .map(offer -> offer.getBaseCostA().getHoverName())
                .toList();
        Component itemName = remainingItemNames.isEmpty()
                ? Component.empty()
                : remainingItemNames.get(random.nextInt(remainingItemNames.size()));

        return Component.translatable(
                "messages.customers.give_up" + messageNumber,
                itemName
        );
    }
}
