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
import net.minecraftforge.items.IItemHandler;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.ForgeItemInsertionTarget;

public final class CustomerPickupCounterForgeEvents {
    static final ResourceLocation ITEM_HANDLER_ID =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "pickup_counter_items");

    private CustomerPickupCounterForgeEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.addGenericListener(
                BlockEntity.class,
                CustomerPickupCounterForgeEvents::attachCapabilities
        );
    }

    static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof CustomerPickupCounterBlockEntity counter) {
            ItemProvider provider = new ItemProvider(counter);
            event.addCapability(ITEM_HANDLER_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    static final class ItemProvider implements ICapabilityProvider {
        private final LazyOptional<IItemHandler> handler;

        private ItemProvider(CustomerPickupCounterBlockEntity counter) {
            handler = LazyOptional.of(() -> new ForgeItemInsertionTarget(counter.getItemInsertionTarget()));
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
