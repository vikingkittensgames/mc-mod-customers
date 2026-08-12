package com.vikingkittens.mc.customers.compatability;

import java.util.List;

import org.mockito.MockedStatic;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import net.neoforged.fml.loading.LoadingModList;

import static org.mockito.Mockito.*;

/**
 * Initializes Minecraft registries for regular unit tests.
 */
public final class NeoForgePlatformTestHelper implements IPlatformTestHelper {
    @Override
    public void bootstrap() {
        LoadingModList modList = mock(LoadingModList.class);
        when(modList.getModFiles()).thenReturn(List.of());
        try (MockedStatic<LoadingModList> mocked = mockStatic(LoadingModList.class)) {
            mocked.when(LoadingModList::get).thenReturn(modList);
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
    }
}
