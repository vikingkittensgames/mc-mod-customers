package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class CustomerForgeClientGameEvents {
    private CustomerForgeClientGameEvents() {}

    @SubscribeEvent
    public static void onBossEventProgress(CustomizeGuiOverlayEvent.BossEventProgress event) {
        CustomerSpawnerSnapshotManager.findByBossEvent(event.getBossEvent().getId()).ifPresent(snapshot -> {
            event.setCanceled(true);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null ||
                    !CustomerBossBarRenderer.isInRange(minecraft.player, snapshot.spawnerPos())) {
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
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CustomerSpawnerSnapshotManager.clear();
        CustomerCounterMarkerManager.clear();
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

    @SubscribeEvent
    public static void renderCounterMarkers(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(event.getPoseStack());
            CustomerCounterMarkerRenderer.render(poseStack, event.getCamera().getPosition());
        }
    }

    private static boolean isNameTagRendered(RenderNameTagEvent event, CustomerVillagerEntity customer, Minecraft minecraft) {
        if (event.getContent() == null || event.getContent().getString().isBlank()) {
            return false;
        }
        if (!ForgeHooksClient.isNameplateInRenderDistance(
                customer,
                minecraft.getEntityRenderDispatcher().distanceToSqr(customer)
        )) {
            return false;
        }
        if (event.getResult() == Event.Result.ALLOW) {
            return true;
        }
        if (event.getResult() != Event.Result.DEFAULT) {
            return false;
        }
        boolean sourceVisibility = customer.shouldShowName() ||
                customer.hasCustomName() && customer == minecraft.getEntityRenderDispatcher().crosshairPickEntity;
        return CustomerWantedItemsRenderer.getDefaultNameTagVisibility(event.getEntity(), sourceVisibility);
    }
}
