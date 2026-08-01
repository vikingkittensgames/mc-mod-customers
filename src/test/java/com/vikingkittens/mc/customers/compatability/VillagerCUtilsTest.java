package com.vikingkittens.mc.customers.compatability;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    @SuppressWarnings("unchecked")
    void appliesTypeAndProfession() {
        VillagerData data = mock(VillagerData.class);
        VillagerData typedData = mock(VillagerData.class);
        VillagerData result = mock(VillagerData.class);
        RegistryAccess registries = mock(RegistryAccess.class);
        Registry<VillagerType> typeRegistry = mock(Registry.class);
        Registry<VillagerProfession> professionRegistry = mock(Registry.class);
        ResourceKey<VillagerType> type = mock(ResourceKey.class);
        ResourceKey<VillagerProfession> profession = mock(ResourceKey.class);
        VillagerType villagerType = mock(VillagerType.class);
        VillagerProfession villagerProfession = mock(VillagerProfession.class);
        when(registries.registryOrThrow(Registries.VILLAGER_TYPE)).thenReturn(typeRegistry);
        when(registries.registryOrThrow(Registries.VILLAGER_PROFESSION)).thenReturn(professionRegistry);
        when(typeRegistry.getOrThrow(type)).thenReturn(villagerType);
        when(professionRegistry.getOrThrow(profession)).thenReturn(villagerProfession);
        when(data.setType(villagerType)).thenReturn(typedData);
        when(typedData.setProfession(villagerProfession)).thenReturn(result);

        assertSame(
                result,
                VillagerCUtils.withTypeAndProfession(data, registries, type, profession)
        );
    }

    @Test
    void checksProfessionKey() {
        VillagerData data = mock(VillagerData.class);
        ResourceKey<VillagerProfession> profession = BuiltInRegistries.VILLAGER_PROFESSION
                .getResourceKey(VillagerProfession.FARMER)
                .orElseThrow();
        when(data.getProfession()).thenReturn(VillagerProfession.FARMER);

        assertTrue(VillagerCUtils.hasProfession(data, profession));
    }
}