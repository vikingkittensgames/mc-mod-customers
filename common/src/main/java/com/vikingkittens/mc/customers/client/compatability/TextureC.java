package com.vikingkittens.mc.customers.client.compatability;

import java.util.Objects;

/**
 * Identifies a texture without exposing a version-specific Minecraft
 * resource identifier.
 *
 * @param namespace resource namespace
 * @param path resource path
 */
public record TextureC(
        String namespace,
        String path
) {
    public TextureC {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");
    }
}
