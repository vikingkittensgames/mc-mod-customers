package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.Map;
import java.util.Optional;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.UseAnim;

final class SkinCustomersVillagerGeckoAnimationSelector {
    private SkinCustomersVillagerGeckoAnimationSelector() {}

    static String locomotion(
            Map<String, String> animations,
            boolean sitting,
            Pose pose,
            boolean fallFlying,
            boolean inWater,
            boolean crouching,
            boolean sprinting,
            boolean moving
    ) {
        String key;
        String fallback;
        if (sitting) {
            key = "sit";
            fallback = "idle";
        } else if (pose == Pose.SLEEPING) {
            key = "sleep";
            fallback = "idle";
        } else if (fallFlying) {
            key = "fall_flying";
            fallback = "idle";
        } else if (inWater) {
            key = "swim";
            fallback = "walk";
        } else if (crouching) {
            key = moving ? "crouch_walk" : "crouch_idle";
            fallback = moving ? "walk" : "idle";
        } else if (moving) {
            key = sprinting ? "run" : "walk";
            fallback = "walk";
        } else {
            key = "idle";
            fallback = "idle";
        }
        return animations.containsKey(key) ? key : fallback;
    }

    static Optional<String> action(
            Map<String, String> animations,
            boolean swinging,
            InteractionHand swingingArm,
            boolean usingItem,
            UseAnim useAnimation,
            boolean holdingChargedCrossbow
    ) {
        String key = null;
        if (swinging) {
            key = swingingArm == InteractionHand.OFF_HAND ? "swing_offhand" : "swing_mainhand";
        } else if (usingItem) {
            key = useAnimation(useAnimation);
            if (!animations.containsKey(key)) {
                key = "use_item";
            }
        } else if (holdingChargedCrossbow) {
            key = "crossbow_hold";
        }
        return key != null && animations.containsKey(key) ? Optional.of(key) : Optional.empty();
    }

    static Optional<String> reaction(
            Map<String, String> animations,
            boolean dying,
            boolean hurt,
            boolean celebrating
    ) {
        if (dying && animations.containsKey("death")) {
            return Optional.of("death");
        }
        if (hurt && animations.containsKey("hurt")) {
            return Optional.of("hurt");
        }
        return celebrating && animations.containsKey("celebrate")
                ? Optional.of("celebrate")
                : Optional.empty();
    }

    private static String useAnimation(UseAnim useAnimation) {
        return switch (useAnimation) {
            case EAT -> "eat";
            case DRINK -> "drink";
            case BLOCK -> "block";
            case BOW -> "bow";
            case SPEAR -> "throw_spear";
            case CROSSBOW -> "crossbow_charge";
            case SPYGLASS -> "spyglass";
            case TOOT_HORN -> "toot_horn";
            case BRUSH -> "brush";
            default -> "use_item";
        };
    }
}
