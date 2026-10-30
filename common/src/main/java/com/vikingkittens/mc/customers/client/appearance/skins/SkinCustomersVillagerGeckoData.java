package com.vikingkittens.mc.customers.client.appearance.skins;

import com.geckolib.constant.dataticket.DataTicket;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerGeckoData {
    static final DataTicket<SkinCustomersVillagerDefinition> DEFINITION =
            DataTicket.create("customers_skin_definition", SkinCustomersVillagerDefinition.class);
    static final DataTicket<SkinCustomersVillagerGeckoProxy> PROXY =
            DataTicket.create("customers_skin_proxy", SkinCustomersVillagerGeckoProxy.class);

    private SkinCustomersVillagerGeckoData() {}
}
