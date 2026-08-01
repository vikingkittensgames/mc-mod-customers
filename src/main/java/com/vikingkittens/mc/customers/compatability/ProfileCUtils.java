package com.vikingkittens.mc.customers.compatability;

import com.mojang.authlib.GameProfile;

/**
 * Provides version-compatible game profile access.
 */
public final class ProfileCUtils {
    private ProfileCUtils() {
    }
    public static String getName(GameProfile profile) {
        return profile.name();
    }
}
