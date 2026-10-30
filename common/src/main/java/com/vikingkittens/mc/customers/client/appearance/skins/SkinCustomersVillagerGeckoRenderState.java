package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.HashMap;
import java.util.Map;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

final class SkinCustomersVillagerGeckoRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> geckoData = new HashMap<>();

    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
        geckoData.put(dataTicket, data);
    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return geckoData.containsKey(dataTicket);
    }

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return geckoData;
    }
}
