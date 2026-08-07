package com.ovrtechnology.aromalib.fabric;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.AromaLibClient;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ClientModInitializer;

public final class AromaLibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(AromaLib.MOD_ID, FabricLoadContext.INSTANCE, AromaLibClient::initialize);
    }
}