package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CustomerGeneratedItemDefinitionsTest {
    private static final List<Path> MODEL_DIRECTORIES = List.of(
            Path.of("../common/src/main/resources/assets/customers/models/item"),
            Path.of("../neoforge/src/generated/resources/assets/customers/models/item")
    );
    private static final Path DEFINITIONS =
            Path.of("../neoforge/src/generated/resources/assets/customers/items");

    @Test
    void definesEveryItemModelForMinecraft12111() throws IOException {
        for (Path modelDirectory : MODEL_DIRECTORIES) {
            try (Stream<Path> models = Files.list(modelDirectory)) {
                for (Path model : models.filter(path -> !path.getFileName().toString().contains("_base"))
                        .filter(path -> !path.getFileName().toString().contains("_overlay"))
                        .toList()) {
                    String fileName = model.getFileName().toString();
                    String name = fileName.substring(0, fileName.length() - ".json".length());
                    JsonObject definition = JsonParser.parseString(Files.readString(DEFINITIONS.resolve(fileName)))
                            .getAsJsonObject();
                    assertEquals(
                            "customers:item/" + name,
                            definition.getAsJsonObject("model").get("model").getAsString()
                    );
                }
            }
        }
    }
}
