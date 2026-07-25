package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

import java.util.List;

public class CustomerSeatEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "customer_seat";
    private static final int EMPTY_DISCARD_TICKS = 20 * 60 * 2;
    private int emptyTicks;

    public CustomerSeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public static boolean isSeat(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.getBlock() instanceof StairBlock
                || blockId != null && isSeatName(blockId.getPath())) {
            return true;
        }

        VoxelShape collisionShape = state.getCollisionShape(level, pos, CollisionContext.empty());
        return !collisionShape.isEmpty()
                && isSeatHeight(getBestSeatHeight(collisionShape));
    }

    static boolean isSeatName(String blockPath) {
        return CustomerSeatLogic.isSeatName(blockPath);
    }

    static boolean isSeatHeight(double height) {
        return CustomerSeatLogic.isSeatHeight(height);
    }

    static double getBestSeatHeight(VoxelShape collisionShape) {
        return CustomerSeatLogic.getBestSeatHeight(collisionShape);
    }

    public static Vec3 getSeatPosition(Level level, BlockPos pos, Entity passenger) {
        for (Entity existingEntity : getEntitiesAt(level, pos, passenger)) {
            if (!(existingEntity instanceof CustomerSeatEntity)) {
                return existingEntity.getPassengerRidingPosition(passenger);
            }
        }

        VoxelShape collisionShape = level.getBlockState(pos).getCollisionShape(
                level,
                pos,
                CollisionContext.of(passenger)
        );
        return new Vec3(
                pos.getX() + 0.5D,
                pos.getY() + getBestSeatHeight(collisionShape),
                pos.getZ() + 0.5D
        );
    }

    public static boolean canSit(Level level, BlockPos pos, Entity passenger) {
        return isSeat(level, pos)
                && getEntitiesAt(level, pos, passenger).stream()
                .allMatch(entity -> entity.getPassengers().isEmpty());
    }

    public static boolean trySit(Level level, BlockPos pos, Entity passenger) {
        if (level.isClientSide() || !canSit(level, pos, passenger)) {
            return false;
        }

        List<CustomerSeatEntity> existingSeats =
                level.getEntitiesOfClass(CustomerSeatEntity.class, new AABB(pos));
        CustomerSeatEntity seat;
        boolean created;
        if (existingSeats.isEmpty()) {
            Vec3 seatPosition = getSeatPosition(level, pos, passenger);
            seat = new CustomerSeatEntity(Customer.CUSTOMER_SEAT.get(), level);
            seat.moveTo(
                    seatPosition.x,
                    seatPosition.y,
                    seatPosition.z,
                    passenger.getYRot(),
                    0.0F
            );
            level.addFreshEntity(seat);
            created = true;
        } else {
            seat = existingSeats.getFirst();
            created = false;
        }

        boolean startedRiding = passenger.startRiding(seat, true);
        if (!startedRiding && created) {
            seat.discard();
        }
        return startedRiding;
    }

    private static List<Entity> getEntitiesAt(
            Level level,
            BlockPos pos,
            Entity excludedEntity
    ) {
        return level.getEntities(
                excludedEntity,
                new AABB(pos),
                entity -> !CustomerSeatLogic.isIgnoredSeatPositionEntityType(
                        entity.getClass()
                )
        );
    }

    static boolean shouldDiscardEmptySeat(int emptyTicks) {
        return CustomerSeatLogic.shouldDiscardEmptySeat(emptyTicks);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (!hasPassenger(passenger)) {
            return;
        }

        Vec3 passengerAttachment = passenger.getVehicleAttachmentPoint(this);
        Vec3 passengerPosition = CustomerSeatLogic.getPassengerPosition(
                position(),
                passengerAttachment
        );
        callback.accept(
                passenger,
                passengerPosition.x,
                passengerPosition.y,
                passengerPosition.z
        );
        if (passenger instanceof LivingEntity livingPassenger) {
            livingPassenger.setYBodyRot(getYRot());
        }
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        if (passenger instanceof LivingEntity livingPassenger) {
            livingPassenger.setYBodyRot(getYRot());
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        emptyTicks = 0;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        emptyTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide()) {
            return;
        }

        if (getPassengers().isEmpty()) {
            emptyTicks++;
            if (shouldDiscardEmptySeat(emptyTicks)) {
                discard();
            }
        } else {
            emptyTicks = 0;
        }
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
    @Override
    public boolean shouldRiderSit() {
        return true;
    }
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return CustomerSeatLogic.getDismountLocation(
                position(),
                getYRot(),
                pos -> level().getBlockState(pos).isAir()
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
