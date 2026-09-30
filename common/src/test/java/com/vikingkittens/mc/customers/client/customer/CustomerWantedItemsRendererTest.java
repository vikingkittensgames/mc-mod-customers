package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.Entity;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CustomerWantedItemsRendererTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void resolvesDirectlyRenderedCustomers() {
        CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);

        assertSame(
                customer,
                CustomerWantedItemsRenderer.getRenderedCustomer(customer)
        );
    }

    @Test
    void resolvesCustomersRenderedThroughAppearanceProxies() {
        CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);
        Entity renderedEntity = mock(
                Entity.class,
                withSettings().extraInterfaces(
                        CustomersVillagerRenderProxy.class
                )
        );
        CustomersVillagerRenderProxy proxy =
                (CustomersVillagerRenderProxy) renderedEntity;
        when(proxy.getCustomersVillagerSource()).thenReturn(customer);

        assertSame(
                customer,
                CustomerWantedItemsRenderer.getRenderedCustomer(renderedEntity)
        );
    }

    @Test
    void ignoresEntitiesThatAreNotCustomersOrCustomerProxies() {
        assertNull(CustomerWantedItemsRenderer.getRenderedCustomer(
                mock(Entity.class)
        ));
    }

    @Test
    void addsNameTagAndAppearanceOffsetsIndependently() {
        assertEquals(
                0.25F,
                CustomerWantedItemsRenderer.getVerticalOffset(false, 0.0F, 9),
                0.0001F
        );
        assertEquals(
                0.595F,
                CustomerWantedItemsRenderer.getVerticalOffset(true, 0.0F, 9),
                0.0001F
        );
        assertEquals(
                0.45F,
                CustomerWantedItemsRenderer.getVerticalOffset(false, 0.2F, 9),
                0.0001F
        );
        assertEquals(
                0.795F,
                CustomerWantedItemsRenderer.getVerticalOffset(true, 0.2F, 9),
                0.0001F
        );
    }
}
