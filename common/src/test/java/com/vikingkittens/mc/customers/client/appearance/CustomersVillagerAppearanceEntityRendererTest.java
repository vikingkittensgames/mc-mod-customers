package com.vikingkittens.mc.customers.client.appearance;

import org.junit.jupiter.api.Test;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomersVillagerAppearanceEntityRendererTest {
    @Test
    void detectsWhetherTheDelegateRendererSubmittedANameTag() {
        EntityRenderState renderState = new EntityRenderState();

        assertFalse(CustomersVillagerAppearanceEntityRenderer.isNameTagRendered(renderState));

        renderState.nameTag = Component.literal("Customer");

        assertTrue(CustomersVillagerAppearanceEntityRenderer.isNameTagRendered(renderState));
    }
}
