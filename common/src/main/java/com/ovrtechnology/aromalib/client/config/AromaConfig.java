package com.ovrtechnology.aromalib.client.config;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.client.config.util.PuffMode;
import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;
import net.blay09.mods.balm.platform.config.reflection.Range;

@Config(value = AromaLib.MOD_ID, type = "client")
public class AromaConfig {

    public General generalSettings = new General();
    public Passive passiveSettings = new Passive();
    public EventSetting weather = new EventSetting();
    public EventSetting playerState = new EventSetting();
    public EventSetting playerProgress = new EventSetting();
    public EventSetting playerAction = new EventSetting();
    public EventSetting combat = new EventSetting();
    public EventSetting craftInteract = new EventSetting();
    public EventSetting dimension = new EventSetting();
    public EventSetting blockBreak = new EventSetting();

    public static class General {
        @Comment("Mode")
        public PuffMode puffMode = PuffMode.AUTOMATIC;

        @Comment("Blah")
        @Range(min = "0", max = "1")
        public double globalIntensityMultiplier = 1.0;

        @Comment("Cooldown in MS")
        @Range(min = "1000", max = "60000")
        public int globalCooldownMs = 3000;

        @Comment("Show persistent tracking while actively tracking")
        public boolean isTrackingToastPersistent = false;

        @Comment("Whether to show the scent borders when triggered")
        public boolean isOverlayActive = true;

        @Comment("Whether it shows debug messages for scent triggers")
        public boolean debugScentMessages = true;

        @Comment("Whether it shows status on title screen")
        public boolean showOmaraStatus = true;
    }

    public static class Passive {
        @Comment("")
        public boolean isPassiveModeActive = true;

        @Range(min = "1000", max = "30000")
        public int blockCooldownMS = 5000;

        @Range(min = "1000", max = "30000")
        public int hostileMobCooldownMS = 8000;

        @Range(min = "1000", max = "30000")
        public int passiveMobCooldownMS = 8000;

        @Range(min = "1.1", max = "5.0")
        public double blockRange = 2.5;

        @Range(min = "1.1", max = "15.0")
        public double passiveMobRange = 5.0;
    }

    public static class EventSetting {
        public boolean isActive = true;

        @Range(min = "1000", max = "60000")
        public int cooldown = 5000;
    }

}
