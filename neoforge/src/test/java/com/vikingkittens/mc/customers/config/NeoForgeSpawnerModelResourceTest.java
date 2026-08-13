package com.vikingkittens.mc.customers.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeoForgeSpawnerModelResourceTest {
    private static final Path MODELS = Path.of(
            "src/main/resources/assets/customers/models/block"
    );

    @Test
    void usesNeoForgeCompositeModels() throws IOException {
        try (Stream<Path> models = Files.list(MODELS)) {
            for (Path model : models.filter(this::isCompositeModel).toList()) {
                assertEquals(
                        "neoforge:composite",
                        JsonParser.parseString(Files.readString(model))
                                .getAsJsonObject()
                                .get("loader")
                                .getAsString(),
                        model.toString()
                );
            }
        }
    }

    private boolean isCompositeModel(Path model) {
        try {
            return JsonParser.parseString(Files.readString(model))
                    .getAsJsonObject()
                    .has("loader");
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
