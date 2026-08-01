package com.vikingkittens.mc.customers.client;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(value = Customers.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomersClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CustomersClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }
}
