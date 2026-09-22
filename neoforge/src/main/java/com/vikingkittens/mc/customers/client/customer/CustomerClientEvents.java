package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    /**
     * Renders customer request groups for customer spawner boss bars.
     *
     * @param event boss bar rendering event
     */
    @SubscribeEvent
    public static void onBossEventProgress(
            CustomizeGuiOverlayEvent.BossEventProgress event
    ) {
        CustomerSpawnerSnapshotManager.findByBossEvent(
                event.getBossEvent().getId()
        ).ifPresent(snapshot -> {
            event.setCanceled(true);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null
                    || !CustomerBossBarRenderer.isInRange(
                            minecraft.player,
                            snapshot.spawnerPos()
                    )) {
                return;
            }
            event.setIncrement(CustomerBossBarRenderer.render(
                    event.getGuiGraphics(),
                    event.getBossEvent(),
                    snapshot,
                    event.getX(),
                    event.getY(),
                    event.getIncrement()
            ));
        });
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        CustomerVillagerEntity customer = CustomerWantedItemsRenderer.getRenderedCustomer(event.getEntity());
        CustomerWantedItemsRenderer.render(
                event.getEntity(),
                customer != null && isNameTagRendered(event, customer, Minecraft.getInstance()),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }

    private static boolean isNameTagRendered(RenderNameTagEvent event, CustomerVillagerEntity customer, Minecraft minecraft) {
        if (event.getContent() == null || event.getContent().getString().isBlank()) {
            return false;
        }

        if (!ClientHooks.isNameplateInRenderDistance(customer, minecraft.getEntityRenderDispatcher().distanceToSqr(customer))) {
            return false;
        }

        if (event.canRender().isTrue()) {
            return true;
        }

        if (!event.canRender().isDefault()) {
            return false;
        }

        boolean sourceVisibility = customer.shouldShowName()
                || customer.hasCustomName()
                        && customer
                                == minecraft.getEntityRenderDispatcher()
                                        .crosshairPickEntity;
        return CustomerWantedItemsRenderer.getDefaultNameTagVisibility(
                event.getEntity(),
                sourceVisibility
        );
    }
}
