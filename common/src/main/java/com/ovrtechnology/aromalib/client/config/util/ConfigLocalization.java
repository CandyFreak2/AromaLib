package com.ovrtechnology.aromalib.client.config.util;

import com.ovrtechnology.aromalib.AromaLib;
import net.blay09.mods.balm.platform.config.schema.BalmConfigSchema;
import net.blay09.mods.balm.platform.config.schema.ConfiguredEnum;
import net.blay09.mods.balm.platform.config.schema.ConfiguredProperty;
import net.blay09.mods.balm.platform.config.schema.builder.ConfigCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public class ConfigLocalization {
    private static final String HEADER = "config.";
    public static String modID;

    public static String forTitle(BalmConfigSchema schema) {
        return HEADER + schema.identifier().getNamespace() + schema.identifier().getPath() + ".title";
    }

    public static Component componentForTitle(BalmConfigSchema schema) {
        return Component.translatable(forTitle(schema));
    }

    public static String forTitle(String modID) {
        return HEADER + modID + ".title";
    }

    public static String forTitle() {
        return forTitle(modID);
    }

    public static Component componentForTitle(String modId) {
        modID = modId;
        return Component.translatable(forTitle(modId));
    }

    public static String forRootCategory(BalmConfigSchema schema) {
        return HEADER + schema.identifier().getNamespace();
    }

    public static String forCategory(ConfigCategory category) {
        return getHeader() + category.parentSchema().identifier().getNamespace() + "." + category.name();
    }

    public static String forCategoryTooltip(ConfigCategory category) {
        return forCategory(category) + ".tooltip";
    }

    public static String forProperty(ConfiguredProperty<?> property) {
        if (property.category().isEmpty()) {
            return getHeader() + property.parentSchema().identifier().getNamespace() + "." + property.name();
        } else {
            return getHeader() + property.parentSchema().identifier().getNamespace() + "." + property.category() + "." + property.name();
        }
    }

    public static Component forEnumValue(ConfiguredEnum<?> property, Enum<?> value) {
        if (value instanceof StringRepresentable stringRepresentable) {
            return Component.translatableWithFallback(forProperty(property) + "." + stringRepresentable.getSerializedName(), stringRepresentable.getSerializedName());
        }

        return Component.translatableWithFallback(forProperty(property) + "." + value.name(), value.name());
    }

    public static Component getProperty(String property) {
        return getProperty(property, "", "");
    }

    public static Component getPropertyWithEnd(String property, String end) {
        return getProperty(property, ".", end);
    }

    public static Component getPropertyWithStart(String property, String start) {
        return getProperty(property, start, "");
    }

    public static Component getProperty(String property, String start, String end) {
        return Component.translatable(getHeader() + property + start  + end);
    }

    public static String getHeader() {
        return HEADER + modID + ".";
    }
}
