package com.vikingkittens.mc.customers.supplier.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.PositionUtils;
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

    private final SupplierVillagerEntity supplier;

    public SupplierMoveToSpawnerGoal(SupplierVillagerEntity supplier, double speedModifier) {
        super(supplier, supplier.getSpawnerPos(), speedModifier);
        this.supplier = supplier;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                supplier.getSpawnerPos() != null &&
                (
                        // Happy path for state flow
                        supplier.getState() == SupplierState.INITIALIZING ||
                        // Non-happy path where movement starts and the path is lost like with a server restart
                        (
                            supplier.getState() == SupplierState.MOVING_TO_SPAWNER &&
                            supplier.getNavigation().getPath() == null
                        )
                );
    }

    @Override
    public void start() {
        targetPos = PositionUtils.findGroundedTargetPosition(supplier.level(), supplier.getSpawnerPos());
        supplier.setState(SupplierState.MOVING_TO_SPAWNER);
        super.start();
    }

    @Override
    public double acceptedDistance() {
        return 1.0;
    }

    @Override
    protected void onDone() {
        mob.snapTo(targetPos.getBottomCenter(), mob.getYRot(), mob.getXRot());
        supplier.setState(SupplierState.SELLING);
        List<Player> players = SearchUtils.findEntitiesInSphere(supplier.level(), Player.class, supplier.blockPosition(), 32, (p, e) -> true);
        Component message = Component.translatable("messages.customers.supplies").withColor(0x36991C);
        for (Player player : players) {
            player.displayClientMessage(message, true);
        }
    }
}
