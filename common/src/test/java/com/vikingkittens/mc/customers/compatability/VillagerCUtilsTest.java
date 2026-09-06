package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VillagerCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void checksProfessionKey() {
        VillagerData data = mock(VillagerData.class);
        ResourceKey<VillagerProfession> profession = VillagerProfession.FARMER;
        Holder<VillagerProfession> professionHolder = mock(Holder.class);
        when(data.profession()).thenReturn(professionHolder);
        when(professionHolder.is(profession)).thenReturn(true);

        assertTrue(VillagerCUtils.hasProfession(data, profession));
    }
}
