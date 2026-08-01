package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class PlayerCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void sendsSystemMessage() {
        Player player = mock(Player.class);
        Component message = Component.literal("System");

        PlayerCUtils.sendSystemMessage(player, message);

        verify(player).displayClientMessage(message, false);
    }
    @Test
    void sendsActionBarMessage() {
        Player player = mock(Player.class);
        Component message = Component.literal("Action");

        PlayerCUtils.sendActionBarMessage(player, message);

        verify(player).displayClientMessage(message, true);
    }
    @Test
    void getsServerLevel() {
        ServerPlayer player = mock(ServerPlayer.class);
        ServerLevel level = mock(ServerLevel.class);
        when(player.level()).thenReturn(level);

        assertSame(level, PlayerCUtils.getServerLevel(player));
    }
}
