package com.ovrtechnology.aromalib;

import com.ovrtechnology.aromalib.websocket.WebSocketConnectionListener;
import com.ovrtechnology.aromalib.hooks.ClientHooks;
import com.ovrtechnology.aromalib.trigger.ScentTriggerHandler;
import com.ovrtechnology.aromalib.trigger.client.PassiveModeHud;
import com.ovrtechnology.aromalib.trigger.client.PathTrackingMaskOverlay;
import com.ovrtechnology.aromalib.trigger.client.ScentPuffOverlay;
import com.ovrtechnology.aromalib.websocket.OvrWebSocketClient;
import com.ovrtechnology.aromalib.websocket.WebSocketConfig;
import lombok.experimental.UtilityClass;
import net.blay09.mods.balm.client.BalmClientRegistrars;

/**
 * Client-side initialization for the Aroma Lib mod.
 * This handles all client-only systems like menus, rendering, and keybindings.
 *
 * <p>Invoked via {@code BalmClient.initializeMod(MOD_ID, loadContext, AromaAffectClient::initialize)}
 * from each platform client entry point.</p>
 */
@UtilityClass
public final class AromaLibClient {

    private static boolean initialized = false;

    /**
     * Initializes all client-side systems.
     * Should be called during client mod initialization.
     */
    public static void initialize(BalmClientRegistrars registrars) {
        if (initialized) {
            AromaLib.LOGGER.warn("AromaLibClient.initialize() called multiple times!");
            return;
        }
        
        AromaLib.LOGGER.info("Initializing Aroma Lib client...");

        ClientHooks.init();

        // Initialize scent trigger handler
        ScentTriggerHandler.init();

        // Initialize passive mode HUD overlay
        PassiveModeHud.init();

        // Initialize tracking HUD (Arrived! notifications + global state tick)
        //TrackingHud.init();

        // Initialize active-tracking scent mask overlay (fullscreen pulse on each path puff)
        PathTrackingMaskOverlay.init();

        // Initialize general scent puff overlay (Omara Device, etc.)
        ScentPuffOverlay.init();

        // Initialize OVR WebSocket client for scent hardware integration
        // The connection is optional - the mod works without it
        initWebSocketClient();

        // TODO: Initialize other client systems
        // - Particle effects
        // - Sound manager
        
        initialized = true;
        AromaLib.LOGGER.info("Aroma Lib client initialized successfully!");
    }

    /**
     * Helper to bridge {@code Holder<MenuType<?>>} (the wildcard form stored in
     * registries) with {@code Holder<MenuType<TMenu>>} required by
     * {@code BalmMenuScreenRegistrar.register}. Safe because the menu type was
     * built with the matching menu class in phase 4.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <TMenu extends net.minecraft.world.inventory.AbstractContainerMenu>
            net.minecraft.core.Holder<net.minecraft.world.inventory.MenuType<TMenu>>
            castMenuHolder(net.minecraft.core.Holder<net.minecraft.world.inventory.MenuType<?>> holder) {
        return (net.minecraft.core.Holder) holder;
    }

    /**
     * Initializes the OVR WebSocket client with default configuration.
     * 
     * <p>The WebSocket connection is optional and used for communicating with
     * OVR's scent hardware. The mod works perfectly without it - the connection
     * will simply retry in the background.</p>
     */
    private static void initWebSocketClient() {
        // TODO: Load config from file when configuration system is set up
        // For now, use default config (localhost:8080)
        WebSocketConfig config = WebSocketConfig.builder()
                .host("127.0.0.1")
                .port(8080)
                .autoConnect(true)       // Try to connect on startup
                .autoReconnect(true)     // Keep trying if connection fails
                .debugLogging(true)      // DEBUG: enabled for troubleshooting OVR connection
                .healthCheckIntervalMs(5000) // 5 seconds health check interval
                .build();
        
        OvrWebSocketClient.init(config);
        
        // Register a listener to log connection events
        OvrWebSocketClient.getInstance().addConnectionListener(
                new WebSocketConnectionListener() {
                    @Override
                    public void onConnected() {
                        AromaLib.LOGGER.info("[OVR] Connected to scent hardware bridge!");
                    }
                    
                    @Override
                    public void onDisconnected(String reason, boolean wasClean) {
                        if (!wasClean) {
                            AromaLib.LOGGER.warn("[OVR] Disconnected from hardware bridge: {}", reason);
                        }
                    }
                    
                    @Override
                    public void onReconnecting(int attemptNumber, long delayMs) {
                        if (attemptNumber == 1) {
                            AromaLib.LOGGER.info("[OVR] Hardware bridge not available, will retry in background...");
                        }
                    }
                }
        );
    }
}

