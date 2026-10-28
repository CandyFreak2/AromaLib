package com.ovrtechnology.aromalib.registry.variants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class TrackerVariantRegistry {
    public static final String DIR = "variants";

    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<Identifier, TrackerDefinition> variants = new LinkedHashMap<>();

    private TrackerVariantRegistry() {}

    public static Map<Identifier, TrackerDefinition> all() {
        return Collections.unmodifiableMap(variants);
    }

    public static Optional<TrackerDefinition> get(Identifier id) {
        return Optional.ofNullable(variants.get(id));
    }

    public static void reload(DataSource source) {
        variants.clear();
        Map<Identifier, JsonElement> discovered = source.listJson(DIR);
        for (Map.Entry<Identifier, JsonElement> entry : discovered.entrySet()) {
            Identifier id = entry.getKey();
            try {
                TrackerDefinition variant = GSON.fromJson(entry.getValue(), TrackerDefinition.class);
                if (variant == null) continue;
                if (variant.getId() == null || variant.getId().isEmpty()) {
                    variant.setId(id.toString());
                }
                variants.put(id, variant);
            } catch (Exception e) {
                AromaLib.LOGGER.error("Failed to parse variant {}: {}", id, e.getMessage());
            }
        }
        AromaLib.LOGGER.info("TrackerVariantRegistry loaded {} variants", variants.size());
    }
}
