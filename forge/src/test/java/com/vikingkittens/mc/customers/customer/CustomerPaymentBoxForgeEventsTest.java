package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.Test;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomerPaymentBoxForgeEventsTest {
    @Test
    void attachesAndInvalidatesItemHandler() {
        CustomerPaymentBoxBlockEntity paymentBox = mock(CustomerPaymentBoxBlockEntity.class);
        AttachCapabilitiesEvent<BlockEntity> event =
                new AttachCapabilitiesEvent<>(BlockEntity.class, paymentBox);

        CustomerPaymentBoxForgeEvents.attachCapabilities(event);

        ICapabilityProvider provider =
                event.getCapabilities().get(CustomerPaymentBoxForgeEvents.ITEM_HANDLER_ID);
        assertNotNull(provider);
        CustomerPaymentBoxForgeEvents.ItemProvider itemProvider =
                assertInstanceOf(CustomerPaymentBoxForgeEvents.ItemProvider.class, provider);
        assertTrue(itemProvider.itemHandler().isPresent());

        event.getListeners().forEach(Runnable::run);

        assertFalse(itemProvider.itemHandler().isPresent());
    }

    @Test
    void ignoresOtherBlockEntities() {
        AttachCapabilitiesEvent<BlockEntity> event =
                new AttachCapabilitiesEvent<>(BlockEntity.class, mock(BlockEntity.class));

        CustomerPaymentBoxForgeEvents.attachCapabilities(event);

        assertTrue(event.getCapabilities().isEmpty());
    }
}
