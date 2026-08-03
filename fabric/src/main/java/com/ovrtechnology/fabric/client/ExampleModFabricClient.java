package com.ovrtechnology.fabric.client;

import com.ovrtechnology.AromaLib;
import com.ovrtechnology.AromaLibClient;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ClientModInitializer;

public final class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(AromaLib.MOD_ID, FabricLoadContext.INSTANCE, AromaLibClient::initialize);

        // Fabric-only renderers that have no Balm wrapper:
        //  - NoseRenderingFabric: Fabric ArmorRenderer hook for the 3D NoseMaskModel
        //  - BlockOutlineRendererFabric: WorldRenderEvents.BEFORE_DEBUG_RENDER hook
        // Both depend on the items / model layers registered above, so they go after
        // BalmClient.initializeMod has run the registrar callbacks.
        NoseRenderingFabric.init();
        BlockOutlineRendererFabric.init();
    }
}
