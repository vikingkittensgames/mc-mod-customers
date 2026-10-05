package com.vikingkittens.mc.customers.client.appearance.skins;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

final class SkinCustomersVillagerGeckoProxy extends Villager implements GeoEntity, CustomersVillagerRenderProxy {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final SkinCustomersVillagerDefinition definition;
    private final Map<String, RawAnimation> loopingAnimations = new HashMap<>();
    private final Map<String, RawAnimation> oneShotAnimations = new HashMap<>();
    private final Map<String, RawAnimation> heldAnimations = new HashMap<>();
    private WeakReference<CustomersVillager> source = new WeakReference<>(null);
    private WeakReference<Mob> sourceEntity = new WeakReference<>(null);
    private int sourceTick = Integer.MIN_VALUE;
    private @Nullable Vec3 overheadAnchor;

    SkinCustomersVillagerGeckoProxy(Level level, SkinCustomersVillagerDefinition definition) {
        super(EntityType.VILLAGER, level);
        this.definition = definition;
    }

    void syncFrom(Mob entity, CustomersVillager villager) {
        source = new WeakReference<>(villager);
        sourceEntity = new WeakReference<>(entity);
        overheadAnchor = null;
        setPos(entity.getX(), entity.getY(), entity.getZ());
        setYRot(entity.getYRot());
        setXRot(entity.getXRot());
        yRotO = entity.yRotO;
        xRotO = entity.xRotO;
        yBodyRot = entity.yBodyRot;
        yBodyRotO = entity.yBodyRotO;
        yHeadRot = entity.yHeadRot;
        yHeadRotO = entity.yHeadRotO;
        tickCount = entity.tickCount;
        hurtTime = entity.hurtTime;
        deathTime = entity.deathTime;
        swinging = entity.swinging;
        swingingArm = entity.swingingArm;
        swingTime = entity.swingTime;
        oAttackAnim = entity.oAttackAnim;
        attackAnim = entity.attackAnim;
        if (sourceTick != entity.tickCount) {
            sourceTick = entity.tickCount;
            walkAnimation.update(entity.walkAnimation.speed(), 1.0F);
        }
        setPose(entity.getPose());
        setDeltaMovement(entity.getDeltaMovement());
        setCustomName(entity.getCustomName());
        setCustomNameVisible(entity.isCustomNameVisible());
        setInvisible(entity.isInvisible());
        setGlowingTag(entity.isCurrentlyGlowing());
        setSprinting(entity.isSprinting());
        setSwimming(entity.isSwimming());
        setShiftKeyDown(entity.isShiftKeyDown());
        setOnGround(entity.onGround());
        setNoGravity(entity.isNoGravity());
        setLeftHanded(entity.isLeftHanded());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setItemSlot(slot, entity.getItemBySlot(slot).copy());
        }
    }

    SkinCustomersVillagerDefinition getDefinition() {
        return definition;
    }

    void setOverheadAnchor(Vec3 overheadAnchor) {
        this.overheadAnchor = overheadAnchor;
    }

    @Nullable Vec3 getOverheadAnchor() {
        return overheadAnchor;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "locomotion", 5, this::locomotion),
                new AnimationController<>(this, "actions", 2, this::action),
                new AnimationController<>(this, "reactions", 0, this::reaction)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public boolean isPassenger() {
        CustomersVillager villager = source.get();
        return villager != null && villager.isVillagerSitting();
    }

    @Override
    public @Nullable Entity getVehicle() {
        Mob entity = sourceEntity.get();
        return entity == null ? null : entity.getVehicle();
    }

    @Override
    public @Nullable CustomersVillager getCustomersVillagerSource() {
        return source.get();
    }

    @Override
    public boolean shouldRenderNameTag() {
        Mob entity = sourceEntity.get();
        return entity != null && entity.shouldShowName();
    }

    private PlayState locomotion(AnimationState<SkinCustomersVillagerGeckoProxy> state) {
        CustomersVillager villager = source.get();
        Mob entity = sourceEntity.get();
        if (villager == null || entity == null) {
            return PlayState.STOP;
        }
        String key = SkinCustomersVillagerGeckoAnimationSelector.locomotion(
                definition.animations(),
                villager.isVillagerSitting(),
                entity.getPose(),
                entity.isFallFlying(),
                villager.isVillagerInWater(),
                entity.isShiftKeyDown(),
                entity.isSprinting(),
                state.isMoving()
        );
        return state.setAndContinue(looping(key));
    }

    private PlayState action(AnimationState<SkinCustomersVillagerGeckoProxy> state) {
        Mob entity = sourceEntity.get();
        if (entity == null) {
            return PlayState.STOP;
        }
        String key = SkinCustomersVillagerGeckoAnimationSelector.action(
                definition.animations(),
                entity.swinging,
                entity.swingingArm,
                entity.isUsingItem(),
                entity.getUseItem().getUseAnimation(),
                CrossbowItem.isCharged(entity.getMainHandItem()) || CrossbowItem.isCharged(entity.getOffhandItem())
        ).orElse(null);
        if (key == null) {
            state.resetCurrentAnimation();
            return PlayState.STOP;
        }
        return state.setAndContinue(oneShot(key));
    }

    private PlayState reaction(AnimationState<SkinCustomersVillagerGeckoProxy> state) {
        Mob entity = sourceEntity.get();
        if (entity == null) {
            return PlayState.STOP;
        }
        boolean celebrating = entity instanceof CustomerVillagerEntity customer
                && customer.getState() == CustomerState.THANKING;
        String key = SkinCustomersVillagerGeckoAnimationSelector.reaction(
                definition.animations(),
                entity.deathTime > 0,
                entity.hurtTime > 0,
                celebrating
        ).orElse(null);
        if (key != null) {
            return state.setAndContinue(key.equals("death") ? held(key) : oneShot(key));
        }
        state.resetCurrentAnimation();
        return PlayState.STOP;
    }

    private RawAnimation looping(String key) {
        return loopingAnimations.computeIfAbsent(
                key,
                ignored -> RawAnimation.begin().thenLoop(definition.animations().get(key))
        );
    }

    private RawAnimation oneShot(String key) {
        return oneShotAnimations.computeIfAbsent(
                key,
                ignored -> RawAnimation.begin().thenPlay(definition.animations().get(key))
        );
    }

    private RawAnimation held(String key) {
        return heldAnimations.computeIfAbsent(
                key,
                ignored -> RawAnimation.begin().thenPlayAndHold(definition.animations().get(key))
        );
    }

}
