package com.vikingkittens.mc.customers.compatability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Provides version-compatible player messaging and level access.
 */
public final class PlayerCUtils {
    private PlayerCUtils() {
    }
    public static void sendSystemMessage(
            Player player,
            Component message
    ) {
        player.displayClientMessage(message, false);
    }
    public static void sendActionBarMessage(
            Player player,
            Component message
    ) {
        player.displayClientMessage(message, true);
    }
    public static ServerLevel getServerLevel(ServerPlayer player) {
        return player.level();
    }
}
