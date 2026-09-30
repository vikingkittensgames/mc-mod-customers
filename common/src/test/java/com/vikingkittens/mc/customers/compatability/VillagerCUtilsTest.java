package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VillagerCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void appliesTypeAndProfession() {
        VillagerData data = mock(VillagerData.class);
        VillagerData typedData = mock(VillagerData.class);
        VillagerData result = mock(VillagerData.class);
        RegistryAccess registries = mock(RegistryAccess.class);
        ResourceKey<VillagerType> type = mock(ResourceKey.class);
        ResourceKey<VillagerProfession> profession = mock(ResourceKey.class);
        when(data.withType(registries, type)).thenReturn(typedData);
        when(typedData.withProfession(registries, profession)).thenReturn(result);

        assertSame(
                result,
                VillagerCUtils.withTypeAndProfession(data, registries, type, profession)
        );
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
