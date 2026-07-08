package com.vikingkittens.mc.customers.supplier.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.supplier.SupplierState;
import com.vikingkittens.mc.customers.supplier.SupplierVillagerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.List;

public class SupplierMoveToSpawnerGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private SupplierVillagerEntity supplier;

    public SupplierMoveToSpawnerGoal(SupplierVillagerEntity supplier, double speedModifier) {
        super(supplier, supplier.getSpawnerPos(), speedModifier);
        this.supplier = supplier;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                supplier.getState() == SupplierState.INITIALIZING &&
                supplier.getSpawnerPos() != null;
    }

    @Override
    public void start() {
        targetPos = MobUtils.getRandomSpawnPos(supplier.level(), supplier.getSpawnerPos(), 3, 3);
        // LOGGER.debug("Target positions: {}", targetPos);
        supplier.setState(SupplierState.MOVING_TO_SPAWNER);
        super.start();
    }

    @Override
    public double acceptedDistance() {
        return 1.5;
    }

    @Override
    protected void onDone() {
        supplier.setState(SupplierState.SELLING);
        List<Player> players = SearchUtils.findEntitiesInSphere(supplier.level(), Player.class, supplier.blockPosition(), 32, (p, e) -> true);
        Component message = Component.translatable("messages.customers.supplies").withColor(0x36991C);
        for (Player player : players) {
            player.displayClientMessage(message, true);
        }
    }
}
