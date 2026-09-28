package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertTrue(CustomerWantedItemsRenderer.getDefaultNameTagVisibility(
                renderedEntity,
                false
        ));

        when(proxy.shouldRenderNameTag()).thenReturn(false);
        assertFalse(CustomerWantedItemsRenderer.getDefaultNameTagVisibility(
                renderedEntity,
                true
        ));
    }

    @Test
    void usesSourceNameTagVisibilityWithoutAProxy() {
        Entity renderedEntity = mock(Entity.class);

        assertTrue(CustomerWantedItemsRenderer.getDefaultNameTagVisibility(
                renderedEntity,
                true
        ));
        assertFalse(CustomerWantedItemsRenderer.getDefaultNameTagVisibility(
                renderedEntity,
                false
        ));
    }

    @Test
    void detectsWhetherTheNameTagIsActuallyRendered() {
        Entity entity = mock(Entity.class);
        when(entity.getDisplayName()).thenReturn(Component.literal("Customer"));

        assertTrue(CustomerWantedItemsRenderer.isNameTagRendered(
                entity,
                true,
                4096.0D
        ));
        assertFalse(CustomerWantedItemsRenderer.isNameTagRendered(
                entity,
                false,
                4096.0D
        ));
        assertFalse(CustomerWantedItemsRenderer.isNameTagRendered(
                entity,
                true,
                4096.1D
        ));

        when(entity.getDisplayName()).thenReturn(Component.empty());
        assertFalse(CustomerWantedItemsRenderer.isNameTagRendered(
                entity,
                true,
                0.0D
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
