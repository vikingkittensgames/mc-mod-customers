package com.vikingkittens.mc.customers.customer;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.config.Config;

@EventBusSubscriber(modid = Customers.MODID)
public class CustomerEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                CustomerShiftFinishedPayload.TYPE,
                CustomerShiftFinishedPayload.STREAM_CODEC,
                CustomerShiftFinishedPayload::handle
        );
        registrar.playToClient(
                CustomerCounterMarkersPayload.TYPE,
                CustomerCounterMarkersPayload.STREAM_CODEC,
                CustomerCounterMarkersPayload::handle
        );
        registrar.playToClient(
                CustomerSpawnerSnapshotPayload.TYPE,
                CustomerSpawnerSnapshotPayload.STREAM_CODEC,
                CustomerSpawnerSnapshotPayload::handle
        );
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(Customer.CUSTOMER_VILLAGER.get(), Villager.createAttributes().build());
        event.put(Customer.CUSTOMER_ZOMBIE.get(), Villager.createAttributes().build());
        event.put(Customer.CUSTOMER_SKELETON.get(), Villager.createAttributes().build());
        event.put(Customer.CUSTOMER_WITCH.get(), Villager.createAttributes().build());
        event.put(Customer.CUSTOMER_HUSK.get(), Villager.createAttributes().build());
        event.put(Customer.CUSTOMER_DROWNED.get(), Villager.createAttributes().build());
        event.put(Customer.CUSTOMER_STRAY.get(), Villager.createAttributes().build());
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!LevelCUtils.isClientSide(event.getLevel())) {
            if (event.getEntity() instanceof CustomerVillagerEntity customer) {
                Entity.RemovalReason reason = customer.getRemovalReason();
                if (reason == Entity.RemovalReason.CHANGED_DIMENSION) {
                    customer.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCustomerInteract(PlayerInteractEvent.EntityInteract event) {
        if (
                Config.ENABLE_QUICK_SELL.get() &&
                event.getHand() == InteractionHand.MAIN_HAND &&
                !LevelCUtils.isClientSide(event.getEntity().level()) &&
                event.getTarget() instanceof CustomerVillagerEntity customer &&
                customer.tryQuickSell(event.getEntity())
        ) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
