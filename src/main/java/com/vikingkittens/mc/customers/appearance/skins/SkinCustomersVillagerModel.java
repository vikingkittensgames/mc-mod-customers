package com.vikingkittens.mc.customers.appearance.skins;

import net.minecraft.util.StringRepresentable;

public enum SkinCustomersVillagerModel implements StringRepresentable {
    WIDE("wide"),
    SLIM("slim");

    public static final StringRepresentable.EnumCodec<SkinCustomersVillagerModel> CODEC =
            StringRepresentable.fromEnum(SkinCustomersVillagerModel::values);

    private final String serializedName;

    SkinCustomersVillagerModel(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
