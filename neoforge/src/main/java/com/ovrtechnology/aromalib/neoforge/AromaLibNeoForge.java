package com.ovrtechnology.aromalib.neoforge;

import com.ovrtechnology.aromalib.AromaLib;
// CURIOS DISABLED (no 26.2 release) — restore these imports + wiring below when Curios publishes for 26.2.
// import com.ovrtechnology.neoforge.accessory.CuriosIntegration;
// import com.ovrtechnology.neoforge.accessory.NoseSlotEnforcer;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(AromaLib.MOD_ID)
public final class AromaLibNeoForge {
    public AromaLibNeoForge(ModContainer modContainer, IEventBus modEventBus) {
        Balm.initializeMod(AromaLib.MOD_ID, new NeoForgeLoadContext(modContainer, modEventBus), AromaLib::initialize);

        // CURIOS DISABLED (no 26.2 release): noses work via the vanilla HEAD slot through
        // NoseAccessoryImpl's fallback path. Restore this block when Curios publishes for 26.2.
        // if (Balm.platform().isModLoaded("curios")) {
        //     modEventBus.addListener(CuriosIntegration::register);
        //     NoseSlotEnforcer.register();
        // }
    }
}
