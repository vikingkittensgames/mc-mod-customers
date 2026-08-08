package com.vikingkittens.mc.customers.appearance;

import net.minecraft.network.chat.Component;

final class DefaultCustomersVillagerAppearance
        implements CustomersVillagerAppearance {
    @Override
    public Component getName() {
        return Component.translatable(
                "appearance.customers.default"
        );
    }
}
