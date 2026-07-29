package com.ovrtechnology.neoforge;

import com.ovrtechnology.config.PuffMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class NeoAromaLibConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<PuffMode> PUFF_MODE = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .defineEnum("puffMode", PuffMode.AUTOMATIC);

    public static final ModConfigSpec.DoubleValue GLOBAL_INTENSITY = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .defineInRange("globalIntensity", 100.0, 0.0, 100.0);

    public static final ModConfigSpec.IntValue GLOBAL_COOLDOWN = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .defineInRange("globalCooldown", 3, 1, 60);

    public static final ModConfigSpec.BooleanValue SHOW_NOSE_RENDER = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("showNoseRender", true);

//    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
//            .comment("A magic number")
//            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);
//
//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
//            .comment("What you want the introduction message to be for the magic number")
//            .define("magicNumberIntroduction", "The magic number is... ");
//
//    // a list of strings that are treated as resource locations for items
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", NeoAromaLibConfig::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
