package com.ovrtechnology.aromalib.trigger.event;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.EventDefinition;
import com.ovrtechnology.aromalib.menu.screen.ScentPuffOverlay;
import com.ovrtechnology.aromalib.menu.screen.SoundTrigger;
import com.ovrtechnology.aromalib.trigger.config.ClientConfig;
import com.ovrtechnology.aromalib.util.Texts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class EventDebugLog {

    private EventDebugLog() {
    }

    public static void fired(EventDefinition def, String scentName, double intensity) {
        if (def == null) return;

        boolean debugChat = ClientConfig.getInstance().isDebugScentMessages();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            // Play sound
            SoundTrigger.playEventSound(player, def.getPerception(), def.getSound());

            if (!debugChat) {
                int intensityPercent = (int) Math.round(intensity * 100);
                String modeTag = def.isContinuous() ? "§dCONT§7" : "§9ONE§7";

                String message = String.format("§a[Event] §e%s §7→ §b%s §8[%d%% · %s · %s]", def.getId(), scentName, intensityPercent, def.getCategory(), modeTag);
                player.sendSystemMessage(Texts.lit(message));
            }
        }

        // Visible feedback (screen-border puff), independent of the debug chat
        // setting — mirrors the proximity-trigger UX so events are observable
        // without OVR hardware.
        if (ClientConfig.getInstance().isPassivePuffOverlay()) {
            ScentPuffOverlay.onScentPuff(scentName, def.getPerception(), intensity);
        }

        if (!debugChat) {
            AromaLib.LOGGER.debug(
                    "[Events] FIRED {} -> {} ({}%, {})",
                    def.getId(),
                    scentName,
                    (int) Math.round(intensity * 100),
                    def.getDurationMode());
            return;
        }

        AromaLib.LOGGER.info(
                "[Events] FIRED {} -> {} ({}%, {} · {} · {})",
                def.getId(),
                scentName,
                (int) Math.round(intensity * 100),
                def.getCategory(),
                def.getTriggerType(),
                def.getDurationMode());
    }

    public static void stopped(EventDefinition def, String scentName) {
        if (def == null) return;
        if (!ClientConfig.getInstance().isDebugScentMessages()) {
            AromaLib.LOGGER.debug("[Events] STOPPED {} (was {})", def.getId(), scentName);
            return;
        }
        AromaLib.LOGGER.info("[Events] STOPPED {} (was {})", def.getId(), scentName);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        String message =
                String.format("§7[Event] §8stopped §7%s §8(%s)", def.getId(), scentName);
        player.sendSystemMessage(Texts.lit(message));
    }

    public static void suppressed(EventDefinition def, String reason) {
        if (def == null) return;
        if (!ClientConfig.getInstance().isDebugScentMessages()) {
            AromaLib.LOGGER.debug("[Events] suppressed {} ({})", def.getId(), reason);
            return;
        }
        AromaLib.LOGGER.debug("[Events] suppressed {} ({})", def.getId(), reason);
    }
}
