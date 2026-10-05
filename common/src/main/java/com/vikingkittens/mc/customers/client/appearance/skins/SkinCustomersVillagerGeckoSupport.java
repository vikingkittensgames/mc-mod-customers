package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

final class SkinCustomersVillagerGeckoSupport {
    private SkinCustomersVillagerGeckoSupport() {}

    static SkinCustomersVillagerGeckoRenderer createRenderer(EntityRendererProvider.Context context) {
        return new SkinCustomersVillagerGeckoEntityRenderer<>(context);
    }
}
