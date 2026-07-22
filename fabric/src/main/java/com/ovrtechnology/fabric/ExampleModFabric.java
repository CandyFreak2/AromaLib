package com.ovrtechnology.fabric;

import com.ovrtechnology.AromaLib;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ModInitializer;

public final class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initializeMod(AromaLib.MOD_ID, FabricLoadContext.INSTANCE, AromaLib::initialize);
    }
}
