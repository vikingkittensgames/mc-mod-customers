package com.vikingkittens.mc.customers;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.mockito.MockedStatic;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Initializes Minecraft registries for regular unit tests.
 */
public final class MinecraftTestBootstrap {
    private static boolean initialized;
    private static MockedStatic<FMLLoader> mockedFmlLoader;

    private MinecraftTestBootstrap() {
    }

    public static synchronized void bootstrap() {
        if (initialized) {
            return;
        }
        LoadingModList modList = mock(LoadingModList.class);
        when(modList.getModFiles()).thenReturn(List.of());
        FMLLoader fmlLoader = mock(FMLLoader.class);
        when(fmlLoader.getDist()).thenReturn(Dist.CLIENT);
        when(fmlLoader.getLoadingModList()).thenReturn(modList);
        when(fmlLoader.isProduction()).thenReturn(false);
        mockedFmlLoader = mockStatic(FMLLoader.class);
        mockedFmlLoader.when(FMLLoader::getCurrent).thenReturn(fmlLoader);
        mockedFmlLoader.when(FMLLoader::getCurrentOrNull).thenReturn(fmlLoader);
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        initialized = true;
    }
}