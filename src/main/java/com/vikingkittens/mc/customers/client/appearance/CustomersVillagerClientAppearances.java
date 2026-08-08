package com.vikingkittens.mc.customers.client.appearance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

public final class CustomersVillagerClientAppearances {
    private static final Map<
                    ResourceLocation,
                    Function<
                            EntityRendererProvider.Context,
                            CustomersVillagerClientAppearance>>
            FACTORIES = new HashMap<>();
    private static final Map<
                    ResourceLocation,
                    CustomersVillagerClientAppearance>
            APPEARANCES = new HashMap<>();

    private CustomersVillagerClientAppearances() {}

    public static void register(
            ResourceLocation appearanceId,
            Function<
                            EntityRendererProvider.Context,
                            CustomersVillagerClientAppearance>
                    factory
    ) {
        FACTORIES.put(appearanceId, factory);
    }

    public static void initialize(
            EntityRendererProvider.Context context
    ) {
        APPEARANCES.clear();
        FACTORIES.forEach((appearanceId, factory) ->
                APPEARANCES.put(
                        appearanceId,
                        factory.apply(context)
                )
        );
    }

    public static @Nullable CustomersVillagerClientAppearance get(
            CustomersVillager villager
    ) {
        return APPEARANCES.get(villager.getAppearanceId());
    }

    public static float getNameTagOffset(
            CustomersVillager villager
    ) {
        CustomersVillagerClientAppearance appearance = get(villager);
        return appearance == null
                ? 0.0F
                : appearance.getNameTagOffset(villager);
    }
}
