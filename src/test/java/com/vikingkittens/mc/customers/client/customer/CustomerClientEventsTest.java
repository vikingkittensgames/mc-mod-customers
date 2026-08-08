package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.Entity;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CustomerClientEventsTest {
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
                CustomerClientEvents.getRenderedCustomer(customer)
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
                CustomerClientEvents.getRenderedCustomer(renderedEntity)
        );
    }

    @Test
    void ignoresEntitiesThatAreNotCustomersOrCustomerProxies() {
        assertNull(CustomerClientEvents.getRenderedCustomer(
                mock(Entity.class)
        ));
    }

    @Test
    void usesTheAppearanceRenderersNameTagVisibilityForProxies() {
        Entity renderedEntity = mock(
                Entity.class,
                withSettings().extraInterfaces(
                        CustomersVillagerRenderProxy.class
                )
        );
        CustomersVillagerRenderProxy proxy =
                (CustomersVillagerRenderProxy) renderedEntity;

        when(proxy.shouldRenderNameTag()).thenReturn(true);
        assertTrue(CustomerClientEvents.getDefaultNameTagVisibility(
                renderedEntity,
                false
        ));

        when(proxy.shouldRenderNameTag()).thenReturn(false);
        assertFalse(CustomerClientEvents.getDefaultNameTagVisibility(
                renderedEntity,
                true
        ));
    }

    @Test
    void usesSourceNameTagVisibilityWithoutAProxy() {
        Entity renderedEntity = mock(Entity.class);

        assertTrue(CustomerClientEvents.getDefaultNameTagVisibility(
                renderedEntity,
                true
        ));
        assertFalse(CustomerClientEvents.getDefaultNameTagVisibility(
                renderedEntity,
                false
        ));
    }
}
