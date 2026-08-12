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

class CustomerPickupCounterForgeEventsTest {
    @Test
    void attachesAndInvalidatesInputHandler() {
        CustomerPickupCounterBlockEntity counter = mock(CustomerPickupCounterBlockEntity.class);
        AttachCapabilitiesEvent<BlockEntity> event = new AttachCapabilitiesEvent<>(BlockEntity.class, counter);

        CustomerPickupCounterForgeEvents.attachCapabilities(event);

        ICapabilityProvider provider =
                event.getCapabilities().get(CustomerPickupCounterForgeEvents.ITEM_HANDLER_ID);
        assertNotNull(provider);
        CustomerPickupCounterForgeEvents.ItemProvider itemProvider =
                assertInstanceOf(CustomerPickupCounterForgeEvents.ItemProvider.class, provider);
        assertTrue(itemProvider.itemHandler().isPresent());

        event.getListeners().forEach(Runnable::run);

        assertFalse(itemProvider.itemHandler().isPresent());
    }

    @Test
    void ignoresOtherBlockEntities() {
        AttachCapabilitiesEvent<BlockEntity> event =
                new AttachCapabilitiesEvent<>(BlockEntity.class, mock(BlockEntity.class));

        CustomerPickupCounterForgeEvents.attachCapabilities(event);

        assertTrue(event.getCapabilities().isEmpty());
    }
}
