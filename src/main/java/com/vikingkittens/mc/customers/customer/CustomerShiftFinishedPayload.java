package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.CustomerClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CustomerShiftFinishedPayload(
        CustomerSpawnerMode spawnerMode,
        float percentComplete,
        int totalCustomers,
        int numCustomersServed,
        int numCustomersGaveUp,
        Map<UUID, Integer> numItemsServedByPlayer
) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<CustomerShiftFinishedPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_shift_finished")
    );

    public static final StreamCodec<FriendlyByteBuf, CustomerShiftFinishedPayload> STREAM_CODEC =
            StreamCodec.of(CustomerShiftFinishedPayload::write, CustomerShiftFinishedPayload::read);

    public CustomerShiftFinishedPayload {
        numItemsServedByPlayer = Map.copyOf(numItemsServedByPlayer);
    }

    private static void write(FriendlyByteBuf buffer, CustomerShiftFinishedPayload payload) {
        buffer.writeEnum(payload.spawnerMode());
        buffer.writeFloat(payload.percentComplete());
        buffer.writeVarInt(payload.totalCustomers());
        buffer.writeVarInt(payload.numCustomersServed());
        buffer.writeVarInt(payload.numCustomersGaveUp());
        buffer.writeMap(
                payload.numItemsServedByPlayer(),
                (target, playerId) -> target.writeUUID(playerId),
                (target, itemCount) -> target.writeVarInt(itemCount)
        );
    }

    private static CustomerShiftFinishedPayload read(FriendlyByteBuf buffer) {
        return new CustomerShiftFinishedPayload(
                buffer.readEnum(CustomerSpawnerMode.class),
                buffer.readFloat(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readMap(
                        HashMap::new,
                        source -> source.readUUID(),
                        source -> source.readVarInt()
                )
        );
    }

    public static void handle(CustomerShiftFinishedPayload payload, IPayloadContext context) {
        LOGGER.debug(payload.toString());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerClientEvents.showCustomerShiftFinishedScreen(payload);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
