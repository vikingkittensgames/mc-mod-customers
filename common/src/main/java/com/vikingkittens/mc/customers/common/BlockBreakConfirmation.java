package com.vikingkittens.mc.customers.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class BlockBreakConfirmation {
    private static final long EXPIRATION_MILLIS = 60_000L;
    private static final Map<UUID, PendingBreak> PENDING_BREAKS = new HashMap<>();

    private BlockBreakConfirmation() {
    }

    public static boolean shouldCancelBreak(ServerPlayer player, BlockPos pos, BlockEntity blockEntity, String titleKey, String messageKey) {
        removeExpiredBreaks();
        PendingBreak confirmedBreak = PENDING_BREAKS.values().stream()
                .filter(pendingBreak -> pendingBreak.matchesBlock(player, pos, blockEntity) && pendingBreak.confirmed)
                .findFirst()
                .orElse(null);
        if (confirmedBreak != null) {
            PENDING_BREAKS.remove(confirmedBreak.token);
            ConfirmationToken.remove(confirmedBreak.token);
            return false;
        }

        ConfirmationToken confirmation = ConfirmationToken.create(player.getUUID(), Util.getMillis(), EXPIRATION_MILLIS);
        PENDING_BREAKS.put(confirmation.token(), new PendingBreak(player.getUUID(), confirmation.token(), pos, blockEntity));
        CustomersServices.network().sendToPlayer(player,
                new BlockBreakConfirmationPromptPayload(player.getUUID(), confirmation.token(), titleKey, messageKey));
        return true;
    }

    public static void confirm(ServerPlayer player, UUID playerId, UUID token) {
        PendingBreak pendingBreak = PENDING_BREAKS.get(token);
        if (pendingBreak == null || !player.getUUID().equals(playerId)
                || !ConfirmationToken.isKnownFor(playerId, token, Util.getMillis())
                || player.level().getBlockEntity(pendingBreak.pos) != pendingBreak.blockEntity) {
            player.sendSystemMessage(Component.translatable("messages.customers.break_confirmation.expired"));
            PENDING_BREAKS.remove(token);
            ConfirmationToken.remove(token);
            return;
        }
        pendingBreak.confirmed = true;
        player.gameMode.destroyBlock(pendingBreak.pos);
    }

    private static void removeExpiredBreaks() {
        long nowMillis = Util.getMillis();
        PENDING_BREAKS.entrySet().removeIf(entry -> !ConfirmationToken.isKnownFor(
                entry.getValue().playerId, entry.getKey(), nowMillis));
    }

    private static final class PendingBreak {
        private final UUID playerId;
        private final UUID token;
        private final BlockPos pos;
        private final BlockEntity blockEntity;
        private boolean confirmed;

        private PendingBreak(UUID playerId, UUID token, BlockPos pos, BlockEntity blockEntity) {
            this.playerId = playerId;
            this.token = token;
            this.pos = pos;
            this.blockEntity = blockEntity;
        }

        private boolean matchesBlock(ServerPlayer player, BlockPos pos, BlockEntity blockEntity) {
            return playerId.equals(player.getUUID()) && this.pos.equals(pos) && this.blockEntity == blockEntity;
        }
    }
}
