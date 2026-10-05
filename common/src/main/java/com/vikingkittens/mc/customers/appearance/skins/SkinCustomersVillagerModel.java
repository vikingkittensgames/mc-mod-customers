package com.vikingkittens.mc.customers.appearance.skins;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

public record SkinCustomersVillagerModel(Type type, @Nullable ResourceLocation resource) {
    public static final SkinCustomersVillagerModel WIDE = new SkinCustomersVillagerModel(Type.WIDE, null);
    public static final SkinCustomersVillagerModel SLIM = new SkinCustomersVillagerModel(Type.SLIM, null);
    public static final Codec<SkinCustomersVillagerModel> CODEC = Codec.STRING.comapFlatMap(
            SkinCustomersVillagerModel::decode,
            SkinCustomersVillagerModel::serializedName
    );

    public SkinCustomersVillagerModel {
        if ((type == Type.GECKO) != (resource != null)) {
            throw new IllegalArgumentException("Only Gecko skin models have a resource ID");
        }
    }

    public static SkinCustomersVillagerModel gecko(ResourceLocation resource) {
        return new SkinCustomersVillagerModel(Type.GECKO, resource);
    }

    public boolean isGecko() {
        return type == Type.GECKO;
    }

    private static DataResult<SkinCustomersVillagerModel> decode(String value) {
        if (value.equals("wide")) {
            return DataResult.success(WIDE);
        }
        if (value.equals("slim")) {
            return DataResult.success(SLIM);
        }
        if (!value.contains(":")) {
            return DataResult.error(() -> "Skin model must be 'wide', 'slim', or a namespaced resource ID: " + value);
        }
        ResourceLocation resource = ResourceLocation.tryParse(value);
        return resource == null
                ? DataResult.error(() -> "Invalid skin model resource ID: " + value)
                : DataResult.success(gecko(resource));
    }

    private String serializedName() {
        return switch (type) {
            case WIDE -> "wide";
            case SLIM -> "slim";
            case GECKO -> resource.toString();
        };
    }

    public enum Type {
        WIDE,
        SLIM,
        GECKO
    }
}
