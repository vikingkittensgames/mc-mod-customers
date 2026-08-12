package com.vikingkittens.mc.customers.client.appearance;

import org.jetbrains.annotations.Nullable;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

public interface CustomersVillagerRenderProxy {
    @Nullable CustomersVillager getCustomersVillagerSource();

    boolean shouldRenderNameTag();
}
