package com.vikingkittens.mc.customers.compatability;

import java.util.List;

import org.mockito.MockedStatic;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;

import static org.mockito.Mockito.*;

/**
 * Initializes Minecraft registries for regular unit tests.
 */
public final class NeoForgePlatformTestHelper implements IPlatformTestHelper {
    private static MockedStatic<FMLLoader> mockedFmlLoader;

    @Override
    public void bootstrap() {
        LoadingModList modList = mock(LoadingModList.class);
        when(modList.getModFiles()).thenReturn(List.of());

        if (mockedFmlLoader == null) {
            FMLLoader loader = mock(FMLLoader.class);
            when(loader.getDist()).thenReturn(Dist.CLIENT);
            when(loader.isProduction()).thenReturn(false);
            when(loader.getLoadingModList()).thenReturn(modList);
            mockedFmlLoader = mockStatic(FMLLoader.class);
            mockedFmlLoader.when(FMLLoader::getCurrent).thenReturn(loader);
            mockedFmlLoader.when(FMLLoader::getCurrentOrNull).thenReturn(loader);
        }

        try (MockedStatic<LoadingModList> mocked = mockStatic(LoadingModList.class)) {
            mocked.when(LoadingModList::get).thenReturn(modList);
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
    }
}
