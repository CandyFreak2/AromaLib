package com.ovrtechnology.aromalib.hooks;

import com.ovrtechnology.aromalib.definition.trigger.ScentTriggerConfigLoader;
import net.blay09.mods.balm.platform.event.callback.ServerLifecycleCallback;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;

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
