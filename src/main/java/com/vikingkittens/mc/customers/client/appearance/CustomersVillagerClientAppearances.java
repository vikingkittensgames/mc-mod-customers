package com.vikingkittens.mc.customers.client.appearance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;

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
    private static final List<CustomersVillagerClientAppearanceProvider>
            PROVIDERS = new ArrayList<>();
    private static final List<InitializedProvider>
            INITIALIZED_PROVIDERS = new ArrayList<>();
    private static @Nullable EntityRendererProvider.Context
            initializationContext;

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

    public static void registerProvider(
            CustomersVillagerClientAppearanceProvider provider
    ) {
        if (PROVIDERS.contains(provider)) {
            return;
        }
        PROVIDERS.add(provider);
        if (initializationContext != null) {
            INITIALIZED_PROVIDERS.add(new InitializedProvider(
                    provider,
                    provider.create(initializationContext)
            ));
        }
    }

    public static void initialize(
            EntityRendererProvider.Context context
    ) {
        initializationContext = context;
        APPEARANCES.clear();
        INITIALIZED_PROVIDERS.clear();
        FACTORIES.forEach((appearanceId, factory) ->
                APPEARANCES.put(
                        appearanceId,
                        factory.apply(context)
                )
        );
        PROVIDERS.forEach(provider ->
                INITIALIZED_PROVIDERS.add(
                        new InitializedProvider(
                                provider,
                                provider.create(context)
                        )
                )
        );
    }

    public static @Nullable CustomersVillagerClientAppearance get(
            CustomersVillager villager
    ) {
        CustomersVillagerClientAppearance registered =
                APPEARANCES.get(villager.getAppearanceId());
        if (registered != null) {
            return registered;
        }
        CustomersVillagerAppearance appearance =
                CustomersVillagerAppearances.get(villager);
        if (appearance == null) {
            return null;
        }
        for (InitializedProvider initialized :
                INITIALIZED_PROVIDERS) {
            if (initialized.provider().supports(appearance)) {
                return initialized.appearance();
            }
        }
        return null;
    }

    public static float getNameTagOffset(
            CustomersVillager villager
    ) {
        CustomersVillagerClientAppearance appearance = get(villager);
        return appearance == null
                ? 0.0F
                : appearance.getNameTagOffset(villager);
    }

    private record InitializedProvider(
            CustomersVillagerClientAppearanceProvider provider,
            CustomersVillagerClientAppearance appearance
    ) {
    }
}
