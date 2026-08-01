package com.vikingkittens.mc.customers.compatability;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mojang.authlib.GameProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
class ProfileCUtilsTest {
    @Test
    void getsProfileName() {
        GameProfile profile = new GameProfile(
                UUID.randomUUID(),
                "Customer"
        );

        assertEquals("Customer", ProfileCUtils.getName(profile));
    }
}
