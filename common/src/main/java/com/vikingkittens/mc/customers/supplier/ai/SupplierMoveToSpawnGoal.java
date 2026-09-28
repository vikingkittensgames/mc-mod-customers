package com.vikingkittens.mc.customers.supplier.ai;

import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.ComponentCUtils;
import com.vikingkittens.mc.customers.compatability.PlayerCUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.supplier.SupplierState;
import com.vikingkittens.mc.customers.supplier.SupplierVillagerEntity;

import java.util.List;

public class SupplierMoveToSpawnGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SupplierVillagerEntity supplier;

    public SupplierMoveToSpawnGoal(SupplierVillagerEntity supplier, double speedModifier) {
        super(supplier, supplier.getSpawnPos(), speedModifier);
        this.supplier = supplier;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                supplier.getSpawnPos() != null &&
                (
                        LevelCUtils.isNighttime(supplier.level()) ||
                        supplier.getOffers().stream().allMatch(MerchantOffer::isOutOfStock)
                ) &&
                (
                        supplier.getState() == SupplierState.SELLING ||
                        (
                                supplier.getState() == SupplierState.MOVING_TO_DESPAWN &&
                                supplier.getNavigation().getPath() == null
                        )
                );
    }

    @Override
    public void start() {
        targetPos = supplier.getSpawnPos();
        supplier.setState(SupplierState.MOVING_TO_DESPAWN);
        if (supplier.getOffers().stream().allMatch(MerchantOffer::isOutOfStock)) {
            List<Player> players = SearchUtils.findEntitiesInSphere(supplier.level(), Player.class, supplier.blockPosition(), 32, (p, e) -> true);
            Component message = ComponentCUtils.withColor(
                    Component.translatable("messages.customers.outofstock"), 0x36991C
            );
            for (Player player : players) {
                PlayerCUtils.sendActionBarMessage(player, message);
            }
        }
        super.start();
    }

    @Override
    public double acceptedDistance() {
        return 1.5;
    }

    @Override
    protected void onDone() {
        supplier.discard();
    }
}
