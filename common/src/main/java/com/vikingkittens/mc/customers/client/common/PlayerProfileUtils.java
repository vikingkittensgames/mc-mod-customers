package com.vikingkittens.mc.customers.client.common;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.ProfileCUtils;

public final class PlayerProfileUtils {
    private PlayerProfileUtils() {}

    public static ResourceLocation getPicture(UUID playerId) {
        if (FakePlayers.isFakePlayer(playerId)) {
            return ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "textures/customers/skins/" + FakePlayers.getName(playerId).toLowerCase() + ".png"
            );
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo playerInfo = connection == null ? null : connection.getPlayerInfo(playerId);
        PlayerSkin skin = playerInfo == null ? DefaultPlayerSkin.get(playerId) : playerInfo.getSkin();
        return skin.texture();
    }

    public static String getName(UUID playerId) {
        if (FakePlayers.isFakePlayer(playerId)) {
            return FakePlayers.getName(playerId);
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo playerInfo = connection == null ? null : connection.getPlayerInfo(playerId);
        GameProfile profile = playerInfo == null ? null : playerInfo.getProfile();
        return profile == null ? playerId.toString().substring(0, 8) : ProfileCUtils.getName(profile);
    }

    public static final class FakePlayers {
        private static final List<String> NAMES = List.of(
                "Alex",
                "Ari",
                "Efe",
                "Herobrine",
                "Makena",
                "Steve",
                "Zuri"
        );

        private FakePlayers() {}

        public static UUID getId(int index) {
            return new UUID(0L, index);
        }

        public static int size() {
            return NAMES.size();
        }

        public static boolean isFakePlayer(UUID playerId) {
            return playerId.getMostSignificantBits() == 0L
                    && playerId.getLeastSignificantBits() >= 0L
                    && playerId.getLeastSignificantBits() < NAMES.size();
        }

        public static String getName(UUID playerId) {
            return NAMES.get((int)playerId.getLeastSignificantBits());
        }
    }
}
