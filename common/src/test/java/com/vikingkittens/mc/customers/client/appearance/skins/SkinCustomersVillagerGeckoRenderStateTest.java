package com.vikingkittens.mc.customers.client.appearance.skins;

import org.junit.jupiter.api.Test;
import software.bernie.geckolib.constant.dataticket.DataTicket;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerGeckoRenderStateTest {
    @Test
    void overridesDataMethodsInjectedIntoTheMinecraftRenderState() throws NoSuchMethodException {
        assertEquals(
                SkinCustomersVillagerGeckoRenderState.class,
                SkinCustomersVillagerGeckoRenderState.class
                        .getDeclaredMethod("addGeckolibData", DataTicket.class, Object.class)
                        .getDeclaringClass()
        );
        assertEquals(
                SkinCustomersVillagerGeckoRenderState.class,
                SkinCustomersVillagerGeckoRenderState.class
                        .getDeclaredMethod("hasGeckolibData", DataTicket.class)
                        .getDeclaringClass()
        );
    }
}
