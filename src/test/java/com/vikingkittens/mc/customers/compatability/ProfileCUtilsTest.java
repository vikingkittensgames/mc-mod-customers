package com.vikingkittens.mc.customers.compatability;

import com.mojang.authlib.GameProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
