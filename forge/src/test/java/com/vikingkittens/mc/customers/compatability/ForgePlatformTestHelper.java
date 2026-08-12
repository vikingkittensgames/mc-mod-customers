package com.vikingkittens.mc.customers.compatability;

import java.util.List;

import org.mockito.MockedStatic;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class ForgePlatformTestHelper implements IPlatformTestHelper {
    @Override
    public void bootstrap() {
        LoadingModList modList = mock(LoadingModList.class);
        when(modList.getModFiles()).thenReturn(List.of());
        try (MockedStatic<LoadingModList> loadingModList = mockStatic(LoadingModList.class);
                MockedStatic<FMLLoader> fmlLoader = mockStatic(FMLLoader.class)) {
            loadingModList.when(LoadingModList::get).thenReturn(modList);
            fmlLoader.when(FMLLoader::getLoadingModList).thenReturn(modList);
            fmlLoader.when(FMLLoader::getGameLayer).thenReturn(ModuleLayer.boot());
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
    }
}
