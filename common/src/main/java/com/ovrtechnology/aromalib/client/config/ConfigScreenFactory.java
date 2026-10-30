package com.ovrtechnology.aromalib.client.config;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.menu.config.MergedBoolIntBoxes;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.platform.config.screen.BalmConfigScreen;
import net.blay09.mods.balm.platform.config.schema.BalmConfigSchema;
import net.blay09.mods.balm.platform.config.schema.ConfiguredProperty;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public class ConfigScreenFactory {
    private static final String KEY_HEADER = "aromalib.config.";

    public static Screen create(Screen parent) {
        final BalmConfigSchema schema = Balm.config().getSchema(AromaConfig.class);
        if (schema == null) {
            return BalmConfigScreen.forMod(parent, AromaLib.MOD_ID);
        }

        return BalmConfigScreen.builder(AromaLib.MOD_ID)
                .section(section("general"), it -> it.properties(List.of(
                        property(schema, "generalSettings", "puffMode"),
                        property(schema, "generalSettings", "globalIntensityMultiplier"),
                        property(schema, "generalSettings", "globalCooldownMs"),
                        property(schema, "generalSettings", "isTrackingToastPersistent"),
                        property(schema, "generalSettings", "isOverlayActive"),
                        property(schema, "generalSettings", "debugScentMessages"),
                        property(schema, "generalSettings", "showOmaraStatus")
                )))
                .section(section("passive_mode"), it -> it.properties(List.of(
                        property(schema, "passiveSettings", "isPassiveModeActive"),
                        property(schema, "passiveSettings", "blockCooldownMS"),
                        property(schema, "passiveSettings", "hostileMobCooldownMS"),
                        property(schema, "passiveSettings", "passiveMobCooldownMS"),
                        property(schema, "passiveSettings", "blockRange"),
                        property(schema, "passiveSettings", "passiveMobRange")
                )))
//                .section(section("events"), it -> it.properties(List.of(
//                        property(schema, "eventsSettings", "areEventTriggersActive"),
//                        property(schema, "eventsSettings", "isWeatherActive"),
//                        property(schema, "eventsSettings", "weatherCooldown"),
//                        property(schema, "eventsSettings", "isPlayerStateActive"),
//                        property(schema, "eventsSettings", "playerStateCooldown"),
//                        property(schema, "eventsSettings", "isPlayerProgressActive"),
//                        property(schema, "eventsSettings", "playerProgressCooldown"),
//                        property(schema, "eventsSettings", "isPlayerActionActive"),
//                        property(schema, "eventsSettings", "playerActionCooldown"),
//                        property(schema, "eventsSettings", "isCombatActive"),
//                        property(schema, "eventsSettings", "combatCooldown"),
//                        property(schema, "eventsSettings", "isCraftingActive"),
//                        property(schema, "eventsSettings", "craftingCooldown"),
//                        property(schema, "eventsSettings", "isDimensionActive"),
//                        property(schema, "eventsSettings", "dimensionCooldown"),
//                        property(schema, "eventsSettings", "isBlockBreakingActive"),
//                        property(schema, "eventsSettings", "blockBreakingCooldown"),
//                        property(schema, "eventsSettings", "globalThrottlePerMinute")
//                )))
                .section(category("events"), it -> it.properties()
                        .mergedProperties(label("weather"), tooltip("weather"), List.of(
                                        intProperty(schema, "weather", "isActive"),
                                        intProperty(schema, "weather", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "weather", "isActive"),
                                        intProperty(schema, "weather", "cooldown")))
                        .mergedProperties(label("playerState"), tooltip("playerState"), List.of(
                                        intProperty(schema, "playerState", "isActive"),
                                        intProperty(schema, "playerState", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "playerState", "isActive"),
                                        intProperty(schema, "playerState", "cooldown")))
                        .mergedProperties(label("playerProgress"), tooltip("playerProgress"), List.of(
                                        intProperty(schema, "playerProgress", "isActive"),
                                        intProperty(schema, "playerProgress", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "playerProgress", "isActive"),
                                        intProperty(schema, "playerProgress", "cooldown")))
                        .mergedProperties(label("playerAction"), tooltip("playerAction"), List.of(
                                        intProperty(schema, "playerAction", "isActive"),
                                        intProperty(schema, "playerAction", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "playerAction", "isActive"),
                                        intProperty(schema, "playerAction", "cooldown")))
                        .mergedProperties(label("combat"), tooltip("combat"), List.of(
                                        intProperty(schema, "combat", "isActive"),
                                        intProperty(schema, "combat", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "combat", "isActive"),
                                        intProperty(schema, "combat", "cooldown")))
                        .mergedProperties(label("craftInteract"), tooltip("craftInteract"), List.of(
                                        intProperty(schema, "craftInteract", "isActive"),
                                        intProperty(schema, "craftInteract", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "craftInteract", "isActive"),
                                        intProperty(schema, "craftInteract", "cooldown")))
                        .mergedProperties(label("dimension"), tooltip("dimension"), List.of(
                                        intProperty(schema, "dimension", "isActive"),
                                        intProperty(schema, "dimension", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "dimension", "isActive"),
                                        intProperty(schema, "dimension", "cooldown")))
                        .mergedProperties(label("blockBreak"), tooltip("blockBreak"), List.of(
                                        intProperty(schema, "blockBreak", "isActive"),
                                        intProperty(schema, "blockBreak", "cooldown")),
                                (screen, _, rowState) -> new MergedBoolIntBoxes(
                                        screen,
                                        booleanProperty(schema, "blockBreak", "isActive"),
                                        intProperty(schema, "blockBreak", "cooldown")))
                )
                .build(parent);
    }

    private static List<ConfiguredProperty<?>> categoryProperties(BalmConfigSchema schema, String category) {
        return Objects.requireNonNull(schema.findCategory(category)).properties();
    }

    private static ConfiguredProperty<?> property(BalmConfigSchema schema, String category, String property) {
        return schema.findProperty(category, property);
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredProperty<Integer> intProperty(BalmConfigSchema schema, String category, String property) {
        return (ConfiguredProperty<Integer>) schema.findProperty(category, property);
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredProperty<List<String>> stringListProperty(BalmConfigSchema schema, String category, String property) {
        return (ConfiguredProperty<List<String>>) schema.findProperty(category, property);
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredProperty<Boolean> booleanProperty(BalmConfigSchema schema, String category, String property) {
        return (ConfiguredProperty<Boolean>) schema.findProperty(category, property);
    }

    private static Component category(String category) {
        return Component.translatable(KEY_HEADER + category);
    }

    private static Component label(String property) {
        return Component.translatable(KEY_HEADER + property);
    }

    private static Component tooltip(String property) {
        return Component.translatable(KEY_HEADER + property + ".tooltip");
    }

    private static Component section(String section) {
        return Component.translatable(KEY_HEADER + "screen." + section).withColor(0x9A7CFF);
    }
}
