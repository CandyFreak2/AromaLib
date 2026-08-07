package com.ovrtechnology.aromalib.neoforge;

import com.ovrtechnology.aromalib.AromaLibClient;
import com.ovrtechnology.aromalib.AromaLib;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge-specific client initialization for Aroma Affect.
 *
 * <p>Most client wiring (model layers, entity renderers, menu screens, key
 * mappings, HUD overlays, OVR WebSocket) goes through Balm via
 * {@link AromaLibClient#initialize}. The hooks that have no Balm wrapper
 * (NeoForge {@code IClientItemExtensions} for armor model providers and the
 * {@code RenderLevelStageEvent.AfterTranslucentBlocks} listener for the path
 * trail / block outline overlays) are wired below.</p>
 */
@Mod(value = AromaLib.MOD_ID, dist = Dist.CLIENT)
public final class AromaLibNeoForgeClient {

    public AromaLibNeoForgeClient(ModContainer modContainer, IEventBus modEventBus) {
        BalmClient.initializeMod(AromaLib.MOD_ID, new NeoForgeLoadContext(modContainer, modEventBus), AromaLibClient::initialize);
    }
}