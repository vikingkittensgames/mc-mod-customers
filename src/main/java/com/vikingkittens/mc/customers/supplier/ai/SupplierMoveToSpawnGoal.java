package com.vikingkittens.mc.customers.supplier.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.supplier.SupplierState;
import com.vikingkittens.mc.customers.supplier.SupplierVillagerEntity;
import org.slf4j.Logger;

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
                supplier.level().isDarkOutside() &&
                (
                        // Happy path for state flow
                        supplier.getState() == SupplierState.SELLING ||
                        // Non-happy path where movement starts and the path is lost like with a server restart
                        (
                                supplier.getState() == SupplierState.MOVING_TO_DESPAWN &&
                                supplier.getNavigation().getPath() == null
                        )
                );
    }

    @Override
    public void start() {
        targetPos = supplier.getSpawnPos();
        // LOGGER.debug("Target positions: {}", targetPos);
        supplier.setState(SupplierState.MOVING_TO_DESPAWN);
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
