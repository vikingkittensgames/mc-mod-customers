package com.vikingkittens.mc.customers.customer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import com.vikingkittens.mc.customers.Customers;

public final class CustomerPaymentBoxForgeEvents {
    static final ResourceLocation ITEM_HANDLER_ID =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "payment_box_items");

    private CustomerPaymentBoxForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addGenericListener(
                BlockEntity.class,
                CustomerPaymentBoxForgeEvents::attachCapabilities
        );
    }

    static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof CustomerPaymentBoxBlockEntity paymentBox) {
            ItemProvider provider = new ItemProvider(paymentBox);
            event.addCapability(ITEM_HANDLER_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    static final class ItemProvider implements ICapabilityProvider {
        private final LazyOptional<IItemHandler> handler;

        private ItemProvider(CustomerPaymentBoxBlockEntity paymentBox) {
            handler = LazyOptional.of(() -> new InvWrapper(paymentBox));
        }

        LazyOptional<IItemHandler> itemHandler() {
            return handler;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(
                @NotNull Capability<T> capability,
                @Nullable Direction direction
        ) {
            return capability == ForgeCapabilities.ITEM_HANDLER ? handler.cast() : LazyOptional.empty();
        }

        private void invalidate() {
            handler.invalidate();
        }
    }
}
