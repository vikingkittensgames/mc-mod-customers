package com.vikingkittens.mc.customers.client.appearance.skins;

import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkinCustomersVillagerGeckoPositionTest {
    @Test
    void copiesCurrentAndPreviousPositionsUsedForRenderingAndLighting() {
        MinecraftTestBootstrap.bootstrap();
        Entity source = mock(Entity.class);
        Entity target = mock(Entity.class);
        when(source.getX()).thenReturn(80.0);
        when(source.getY()).thenReturn(72.0);
        when(source.getZ()).thenReturn(-40.0);
        Vec3 previousPosition = new Vec3(79.25, 71.75, -39.5);
        when(source.getPosition(0.0F)).thenReturn(previousPosition);
        source.yRotO = 35.0F;
        source.xRotO = -10.0F;

        SkinCustomersVillagerGeckoProxy.syncPosition(source, target);

        verify(target).setPos(80.0, 72.0, -40.0);
        verify(target).setOldPosAndRot(previousPosition, 35.0F, -10.0F);
    }
}
