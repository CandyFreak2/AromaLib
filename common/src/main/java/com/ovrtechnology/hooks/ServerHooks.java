package com.ovrtechnology.hooks;

import com.ovrtechnology.trigger.config.ClientConfig;
import com.ovrtechnology.trigger.config.ScentTriggerConfigLoader;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.blay09.mods.balm.platform.event.callback.ServerLifecycleCallback;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.minecraft.client.gui.screens.TitleScreen;

public class ServerHooks {
    public static void register() {
        ServerPlayerCallback.Join.EVENT.register(player -> {
            ScentTriggerConfigLoader.init();
        });

        ServerLifecycleCallback.Reloaded.EVENT.register(_ -> {
            ScentTriggerConfigLoader.rebuild();
        });
    }
}
