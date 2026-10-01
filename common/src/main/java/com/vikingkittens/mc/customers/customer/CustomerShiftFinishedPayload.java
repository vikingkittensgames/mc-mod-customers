package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.CustomersNetworkPayload;

public record CustomerShiftFinishedPayload(
        CustomerSpawnerMode spawnerMode,
        float percentComplete,
        boolean levelPassed,
        int totalCustomers,
        int numCustomersServed,
        int numCustomersGaveUp,
        Map<UUID, Integer> numItemsServedByPlayer,
        Map<UUID, Integer> numItemsCraftedByPlayer,
        int numItemsServedAutomated,
        int numItemsCraftedAutomated
) implements CustomersNetworkPayload {
    public static final ResourceLocation ID = new ResourceLocation(Customers.MODID, "customer_shift_finished");

    public CustomerShiftFinishedPayload {
        numItemsServedByPlayer = Map.copyOf(numItemsServedByPlayer);
        numItemsCraftedByPlayer = Map.copyOf(numItemsCraftedByPlayer);
    }

    /**
     * Returns the total item units served during the shift.
     *
     * @return served item total
     */
    public int totalItemsServed() {
        return numItemsServedAutomated
                + numItemsServedByPlayer.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Returns the total item units crafted during the shift.
     *
     * @return crafted item total
     */
    public int totalItemsCrafted() {
        return numItemsCraftedAutomated
                + numItemsCraftedByPlayer.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(spawnerMode());
        buffer.writeFloat(percentComplete());
        buffer.writeBoolean(levelPassed());
        buffer.writeVarInt(totalCustomers());
        buffer.writeVarInt(numCustomersServed());
        buffer.writeVarInt(numCustomersGaveUp());
        buffer.writeMap(
                numItemsServedByPlayer(),
                (target, playerId) -> target.writeUUID(playerId),
                (target, itemCount) -> target.writeVarInt(itemCount)
        );
        buffer.writeMap(
                numItemsCraftedByPlayer(),
                (target, playerId) -> target.writeUUID(playerId),
                (target, itemCount) -> target.writeVarInt(itemCount)
        );
        buffer.writeVarInt(numItemsServedAutomated());
        buffer.writeVarInt(numItemsCraftedAutomated());
    }

    public static CustomerShiftFinishedPayload read(FriendlyByteBuf buffer) {
        return new CustomerShiftFinishedPayload(
                buffer.readEnum(CustomerSpawnerMode.class),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readMap(
                        HashMap::new,
                        source -> source.readUUID(),
                        source -> source.readVarInt()
                ),
                buffer.readMap(
                        HashMap::new,
                        source -> source.readUUID(),
                        source -> source.readVarInt()
                ),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
