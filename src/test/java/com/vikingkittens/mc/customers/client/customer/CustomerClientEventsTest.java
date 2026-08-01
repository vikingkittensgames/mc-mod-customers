package com.vikingkittens.mc.customers.client.customer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerClientEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void extractsCustomerAndSittingRenderData() {
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        when(customer.isCustomerSitting()).thenReturn(true);
        EntityRenderState renderState = new EntityRenderState();

        CustomerClientEvents.extractCustomerRenderData(customer, renderState);

        assertSame(customer, renderState.getRenderData(CustomerClientEvents.CUSTOMER_RENDER_DATA));
        assertTrue(renderState.getRenderData(CustomerClientEvents.CUSTOMER_SITTING_RENDER_DATA));
    }
}