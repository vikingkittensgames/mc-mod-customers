package com.vikingkittens.mc.customers.appearance.skins;

public enum SkinCustomersVillagerSound {
    AMBIENT("ambient"),
    HURT("hurt"),
    DEATH("death"),
    STEP("step");

    private final String serializedName;

    SkinCustomersVillagerSound(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }
}
