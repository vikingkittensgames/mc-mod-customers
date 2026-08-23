package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

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
) {

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

    public static void write(FriendlyByteBuf buffer, CustomerShiftFinishedPayload payload) {
        buffer.writeEnum(payload.spawnerMode());
        buffer.writeFloat(payload.percentComplete());
        buffer.writeBoolean(payload.levelPassed());
        buffer.writeVarInt(payload.totalCustomers());
        buffer.writeVarInt(payload.numCustomersServed());
        buffer.writeVarInt(payload.numCustomersGaveUp());
        buffer.writeMap(
                payload.numItemsServedByPlayer(),
                (target, playerId) -> target.writeUUID(playerId),
                (target, itemCount) -> target.writeVarInt(itemCount)
        );
        buffer.writeMap(
                payload.numItemsCraftedByPlayer(),
                (target, playerId) -> target.writeUUID(playerId),
                (target, itemCount) -> target.writeVarInt(itemCount)
        );
        buffer.writeVarInt(payload.numItemsServedAutomated());
        buffer.writeVarInt(payload.numItemsCraftedAutomated());
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

}
