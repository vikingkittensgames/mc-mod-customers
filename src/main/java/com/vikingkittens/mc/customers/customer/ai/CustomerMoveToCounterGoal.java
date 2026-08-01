package com.vikingkittens.mc.customers.customer.ai;

import java.util.*;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.customer.*;

public class CustomerMoveToCounterGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CustomerVillagerEntity customer;
    private BlockPos counterPosition;

    public CustomerMoveToCounterGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, null, speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        CustomerSpawnerBlockEntity spawner = customer.getSpawner();
        return super.canUse() && spawner != null &&
                (
                        customer.getState() == CustomerState.INITIALIZING ||
                        (
                                (
                                        customer.getState() == CustomerState.LINING_UP ||
                                        customer.getState() == CustomerState.IN_LINE
                                ) &&
                                customer.getCounterTargetBlockPos() == null
                        ) ||
                        (
                                customer.getState() == CustomerState.IN_LINE &&
                                customer.getCounterTargetBlockPos() != null &&
                                spawner.getReservedTargetCounterPositionFollowingCustomerId(
                                        customer.getCounterTargetBlockPos(),
                                        customer.getUUID()
                                ) == null
                        ) ||
                        (
                                customer.getState() == CustomerState.MOVING_TO_COUNTER &&
                                customer.getNavigation().getPath() == null
                        )
                ) &&
                customer.getCounterBlockState() != null &&
                !customer.getCounterBlockState().isAir();
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return super.isValidTarget(levelReader, blockPos) && (
                customer.getState() == CustomerState.INITIALIZING ||
                customer.getState() == CustomerState.MOVING_TO_COUNTER
        );
    }

    @Override
    public void start() {
        targetPos = null;
        CustomerSpawnerBlockEntity spawner = customer.getSpawner();
        if (spawner != null) {
            targetPos = customer.getCounterTargetBlockPos();

            List<BlockPos> counterPositions = CustomerCounter.findCounterPositions(
                    customer.level(),
                    customer.getSpawnerPos(),
                    customer.getCounterBlockState()
            );
            List<CustomerCounter.SurroundingPosition> validPositions = CustomerCounter.findValidSurroundingPositions(
                    customer.level(),
                    counterPositions,
                    customer,
                    customer.getAvoidBlockState()
            );

            if (!validPositions.isEmpty()) {
                CustomerCounter.SurroundingPosition surroundingPos = validPositions.stream()
                        .filter(sp -> sp.getPosition().equals(targetPos))
                        .findFirst()
                        .orElse(null);
                if (surroundingPos == null) {
                    RandomSource random = customer.level().getRandom();
                    Util.shuffle(validPositions, random);
                    Map<BlockPos, List<UUID>> reservedTargetCounterPositions =
                            spawner.getReservedTargetCounterPositions();
                    validPositions.sort(
                            Comparator
                                    .comparingInt((CustomerCounter.SurroundingPosition position) ->
                                            reservedTargetCounterPositions
                                                    .getOrDefault(
                                                            position.getPosition(),
                                                            List.of()
                                                    )
                                                    .size()
                                    )
                                    .thenComparing(position ->
                                            !CustomerSeatEntity.canSit(customer.level(), position.getPosition().below(), customer))
                                    .thenComparingDouble(CustomerCounter.SurroundingPosition::getDistanceSqr)
                    );

                    List<CustomerCounter.SurroundingPosition> untargetedPositions = new ArrayList<>();
                    List<CustomerCounter.SurroundingPosition> untargetedNotTooClosePositions = new ArrayList<>();
                    Set<BlockPos> otherCustomersTargetPositions =
                            reservedTargetCounterPositions.keySet();
                    for (CustomerCounter.SurroundingPosition sp : validPositions) {
                        if (otherCustomersTargetPositions.stream().noneMatch(
                                pos -> pos.equals(sp.getPosition())
                        )) {
                            untargetedPositions.add(sp);
                            if (otherCustomersTargetPositions.stream().noneMatch(
                                    pos -> pos.distToCenterSqr(
                                            sp.getPosition().getBottomCenter()
                                    ) < 3 * 3
                            )) {
                                untargetedNotTooClosePositions.add(sp);
                            }
                        }
                    }

                    if (!untargetedNotTooClosePositions.isEmpty()) {
                        surroundingPos = untargetedNotTooClosePositions.getFirst();
                    } else if (!untargetedPositions.isEmpty()) {
                        surroundingPos = untargetedPositions.getFirst();
                    } else {
                        surroundingPos = validPositions.getFirst();
                    }
                }
                targetPos = surroundingPos.getPosition();
                counterPosition = surroundingPos.getCenter();
                customer.setCounterTargetBlockPos(targetPos);
                UUID leadCustomerId = spawner.tryReserveTargetCounterPosition(targetPos, customer.getUUID());
                if (leadCustomerId.equals(customer.getUUID())) {
                    customer.setState(CustomerState.MOVING_TO_COUNTER);
                    super.start();
                } else {
                    customer.setState(CustomerState.LINING_UP);
                }
            } else {
                customer.setState(CustomerState.DONE);
            }
        }
    }

    @Override
    protected void onDone() {
        if (customer.getState() == CustomerState.MOVING_TO_COUNTER) {
            EntityCUtils.snapTo(
                    mob,
                    targetPos.getBottomCenter(),
                    mob.getYRot(),
                    mob.getXRot()
            );
            if (counterPosition != null) {
                customer.lookAt(EntityAnchorArgument.Anchor.EYES, counterPosition.getCenter());
            }
            CustomerSeatEntity.trySit(customer.level(), targetPos.below(), customer);
            customer.setState(CustomerState.BUYING);
        }
    }
}
