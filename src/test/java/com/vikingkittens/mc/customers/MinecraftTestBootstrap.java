package com.vikingkittens.mc.customers;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.mockito.MockedStatic;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Initializes Minecraft registries for regular unit tests.
 */
public final class MinecraftTestBootstrap {
    private static boolean initialized;

    private MinecraftTestBootstrap() {
    }

    /**
     * Initializes Minecraft once for the current test JVM.
     */
    public static synchronized void bootstrap() {
        if (initialized) {
            return;
        }
        LoadingModList modList = mock(LoadingModList.class);
        when(modList.getModFiles()).thenReturn(List.of());
        try (MockedStatic<LoadingModList> mocked = mockStatic(LoadingModList.class)) {
            mocked.when(LoadingModList::get).thenReturn(modList);
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
        initialized = true;
    }
}
