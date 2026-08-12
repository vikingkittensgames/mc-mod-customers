package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

@EventBusSubscriber(modid = Customers.MODID)
public class CustomerEvents {
    /**
     * Routes sneaking pickup-counter interactions to the block instead of
     * allowing the held item to handle them.
     *
     * @param event block interaction event
     */
    @SubscribeEvent
    public static void onPickupCounterInteract(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (CustomerInteractions.shouldUsePickupCounter(event.getEntity(), event.getLevel(), event.getPos())) {
            event.setUseBlock(TriState.TRUE);
            event.setUseItem(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                CustomerShiftFinishedPayload.TYPE,
                CustomerShiftFinishedPayload.STREAM_CODEC,
                CustomerPayloadHandlers::handleShiftFinished
        );
        registrar.playToClient(
                CustomerCounterMarkersPayload.TYPE,
                CustomerCounterMarkersPayload.STREAM_CODEC,
                CustomerPayloadHandlers::handleCounterMarkers
        );
        registrar.playToClient(
                CustomerSpawnerSnapshotPayload.TYPE,
                CustomerSpawnerSnapshotPayload.STREAM_CODEC,
                CustomerPayloadHandlers::handleSpawnerSnapshot
        );
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(Customer.CUSTOMER_VILLAGER.get(), Villager.createAttributes().build());
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
        if (CustomerInteractions.tryQuickSell(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
