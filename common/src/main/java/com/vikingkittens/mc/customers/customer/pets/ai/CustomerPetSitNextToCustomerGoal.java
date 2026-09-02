package com.vikingkittens.mc.customers.customer.pets.ai;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;

import com.vikingkittens.mc.customers.common.PositionUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public final class CustomerPetSitNextToCustomerGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double MOVE_SPEED = 1.0D;

    private final Animal pet;
    private final UUID customerId;
    private CustomerVillagerEntity customer;
    private boolean used;

    public CustomerPetSitNextToCustomerGoal(Animal pet, UUID customerId) {
        super(pet, null, MOVE_SPEED);
        this.pet = pet;
        this.customerId = customerId;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !used && super.canUse() &&
                findBuyingCustomer();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() &&
                findBuyingCustomer();
    }

    @Override
    public void start() {
        used = true;
        targetPos = findTargetPosition();
        if (targetPos == null) {
            return;
        }
        if (pet instanceof TamableAnimal tamablePet) {
            tamablePet.setOrderedToSit(false);
        }
        super.start();
    }

    @Override
    protected void onDone() {
        if (targetPos != null && findBuyingCustomer()) {
            LOGGER.debug("$$$$$$$ Pet ready to sit {} -- {}", pet, targetPos);
            EntityCUtils.snapTo(pet, targetPos.getBottomCenter(), pet.getYRot(), pet.getXRot());
            pet.getLookControl().setLookAt(customer, 10.0F, pet.getMaxHeadXRot());
            if (pet instanceof TamableAnimal tamablePet) {
                LOGGER.debug("$$$$$$$ Telling {} to sit", pet);
                tamablePet.setOrderedToSit(true);
            }
        }
    }

    static int getPositionScore(Direction customerFacing, Direction positionDirection) {
        if (positionDirection == customerFacing.getClockWise() ||
                positionDirection == customerFacing.getCounterClockWise()) {
            return 0;
        }
        return positionDirection == customerFacing.getOpposite() ? 1 : 2;
    }

    private boolean findBuyingCustomer() {
        customer = findCustomer();
        return customer != null && customer.getState() == CustomerState.BUYING;
    }

    private @Nullable BlockPos findTargetPosition() {
        if (customer == null) {
            return null;
        }
        int customerGroundY = customer.getBlockY() - (customer.isVillagerSitting() ? 1 : 0);
        List<CandidatePosition> candidates = Direction.Plane.HORIZONTAL.stream()
                .map(direction -> createCandidatePosition(direction, customer.blockPosition()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(candidate ->
                        Math.abs(candidate.position().getY() - customerGroundY)))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }

        int closestY = candidates.getFirst().position().getY();
        Direction customerFacing = customer.getDirection();
        return candidates.stream()
                .filter(candidate -> candidate.position().getY() == closestY)
                .min(Comparator.comparingInt(candidate -> getPositionScore(customerFacing, candidate.direction())))
                .map(CandidatePosition::position)
                .orElse(null);
    }

    private @Nullable CandidatePosition createCandidatePosition(Direction direction, BlockPos customerPosition) {
        BlockPos position = PositionUtils.findGroundedTargetPosition(
                pet.level(),
                customerPosition.relative(direction)
        );
        return position == null ? null : new CandidatePosition(direction, position);
    }

    private CustomerVillagerEntity findCustomer() {
        if (pet.level() instanceof ServerLevel serverLevel &&
                serverLevel.getEntity(customerId) instanceof CustomerVillagerEntity customer &&
                customer.isAlive()) {
            return customer;
        }
        return null;
    }

    private record CandidatePosition(Direction direction, BlockPos position) {}
}
