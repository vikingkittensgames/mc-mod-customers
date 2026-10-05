package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.UseAnim;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerGeckoAnimationSelectorTest {
    private static final Map<String, String> REQUIRED = Map.of(
            "idle", "misc.idle",
            "walk", "move.walk",
            "sit", "pose.sit"
    );

    @Test
    void selectsRequiredLocomotionAndDocumentedFallbacks() {
        assertEquals("sit", locomotion(REQUIRED, true, false, false, false, false));
        assertEquals("walk", locomotion(REQUIRED, false, false, false, true, true));
        assertEquals("walk", locomotion(REQUIRED, false, false, true, false, true));
        assertEquals("walk", locomotion(REQUIRED, false, false, true, false, false));
        assertEquals("idle", locomotion(REQUIRED, false, true, false, false, false));
    }

    @Test
    void selectsOptionalLocomotionWhenDefined() {
        Map<String, String> animations = Map.of(
                "idle", "idle",
                "walk", "walk",
                "sit", "sit",
                "run", "run",
                "sleep", "sleep"
        );

        assertEquals("run", locomotion(animations, false, false, false, true, true));
        assertEquals("sleep", locomotion(animations, false, true, false, false, false));
    }

    @Test
    void specializesItemUseAndFallsBackToGenericUse() {
        Map<String, String> specialized = Map.of("eat", "eat");
        Map<String, String> generic = Map.of("use_item", "use");

        assertEquals(Optional.of("eat"), action(specialized, true, UseAnim.EAT, false));
        assertEquals(Optional.of("use_item"), action(generic, true, UseAnim.EAT, false));
        assertEquals(Optional.empty(), action(Map.of(), true, UseAnim.EAT, false));
    }

    @Test
    void prioritizesDeathThenHurtThenCelebration() {
        Map<String, String> animations = Map.of("death", "death", "hurt", "hurt", "celebrate", "celebrate");

        assertEquals(Optional.of("death"), reaction(animations, true, true, true));
        assertEquals(Optional.of("hurt"), reaction(animations, false, true, true));
        assertEquals(Optional.of("celebrate"), reaction(animations, false, false, true));
    }

    private static String locomotion(
            Map<String, String> animations,
            boolean sitting,
            boolean sleeping,
            boolean inWater,
            boolean sprinting,
            boolean moving
    ) {
        return SkinCustomersVillagerGeckoAnimationSelector.locomotion(
                animations,
                sitting,
                sleeping ? Pose.SLEEPING : Pose.STANDING,
                false,
                inWater,
                false,
                sprinting,
                moving
        );
    }

    private static Optional<String> action(
            Map<String, String> animations,
            boolean usingItem,
            UseAnim useAnimation,
            boolean chargedCrossbow
    ) {
        return SkinCustomersVillagerGeckoAnimationSelector.action(
                animations,
                false,
                InteractionHand.MAIN_HAND,
                usingItem,
                useAnimation,
                chargedCrossbow
        );
    }

    private static Optional<String> reaction(
            Map<String, String> animations,
            boolean dying,
            boolean hurt,
            boolean celebrating
    ) {
        return SkinCustomersVillagerGeckoAnimationSelector.reaction(animations, dying, hurt, celebrating);
    }
}
