package com.ovrtechnology.aromalib.definition;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.regex.Pattern;

public abstract class AbstractDefinition {
    /**
     * Pattern for validating HTML hex color format.
     * Matches #RGB, #RRGGBB, or #RRGGBBAA formats.
     */
    protected static final Pattern HTML_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$");

    /**
     * The ID (e.g., "minecraft:diamond_ore").
     * This is the primary identifier and must be unique.
     */
    @SerializedName("id")
    @Getter
    protected String id;

    /**
     * Default constructor for GSON deserialization.
     */
    public AbstractDefinition() {
    }

    /**
     * Constructor for programmatic creation.
     *
     * @param id Minecraft ID
     */
    public AbstractDefinition(String id) {
        this.id = id;
    }

    /**
     * Format an ID as a display name.
     * Example: "minecraft:diamond_ore" -> "Diamond Ore"
     *
     * @param id The ID to format
     * @return Formatted display name
     */
    protected static String formatIDAsName(String id) {
        // Remove namespace
        String path = id.contains(":") ? id.split(":", 2)[1] : id;

        // Convert underscores to spaces and capitalize
        String[] parts = path.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                if (!result.isEmpty()) {
                    result.append(" ");
                }
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    /**
     * Get the localization key for this definition's display name.
     * Uses Minecraft's definition translation keys.
     *
     * @return Localization key in format "block.namespace.path"
     */
    protected String getTranslationKey() {
        if (id == null || !id.contains(":")) {
            return getTranslationTitle() + ".aromalib.unknown";
        }
        String[] parts = id.split(":", 2);
        return getTranslationTitle() + "." + parts[0] + "." + parts[1];
    }

    protected String getDescTranslationKey() {
        return getTranslationKey() + "." + getDescriptionKey();
    }

    /**
     * Get the namespace portion of the definition ID.
     *
     * @return Namespace (e.g., "minecraft"), or empty string if invalid
     */
    public String getNamespace() {
        if (id != null && id.contains(":")) {
            return id.split(":", 2)[0];
        }
        return "";
    }

    public boolean isVanilla() {
        return "minecraft".equals(getNamespace());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTriggerDefinition that = (AbstractTriggerDefinition) o;
        return id != null && id.equals(that.id);
    }

    /**
     * Validates the definition has required fields.
     *
     * @return true if the definition is valid
     */
    public boolean isValid() {
        return id != null && !id.isEmpty();
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    protected String getFallbackTitle() {
        return "";
    }

    protected String getTranslationTitle() {
        return "";
    }

    protected String getDescriptionKey() {
        return "";
    }
}
