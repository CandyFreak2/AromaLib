package com.ovrtechnology.aromalib.definition;

import com.google.gson.annotations.SerializedName;
import com.ovrtechnology.aromalib.util.SoundRef;
import lombok.Getter;

public abstract class AbstractTriggerDefinition extends AbstractDefinition {
    /**
     * HTML hex color for UI display (e.g., "#5DECF5").
     * Used for rendering block indicators, trails, and menu items.
     */
    @SerializedName("color")
    protected String colorHtml;

    /**
     * Optional fallback display name when localization is unavailable.
     * If not provided, the ID will be formatted as the name.
     */
    @SerializedName("fallback_name")
    protected String fallbackName;

    @SerializedName("perception")
    @Getter
    protected String perception;

    @SerializedName("sound")
    @Getter
    protected SoundRef sound;

    /**
     * Default constructor for GSON deserialization.
     */
    public AbstractTriggerDefinition() {
        super();
    }

    /**
     * Constructor for programmatic creation.
     *
     * @param id Minecraft ID
     * @param colorHtml HTML hex color
     */
    public AbstractTriggerDefinition(String id, String fallbackName, String colorHtml) {
        super(id);
        this.fallbackName = fallbackName;
        this.colorHtml = colorHtml;
    }

    /**
     * Get the color with validation and fallback.
     *
     * @return Valid HTML color, or default white if invalid
     */
    public String getColorHtml() {
        if (isValidHtmlColor(colorHtml)) {
            return colorHtml.toUpperCase();
        }
        return getDefaultColor();
    }

    /**
     * Get the raw color value without validation.
     *
     * @return The raw color value as stored
     */
    public String getRawColorHtml() {
        return colorHtml;
    }

    /**
     * Parse the HTML color to an integer RGB value.
     *
     * @return RGB color as integer (0xRRGGBB format)
     */
    public int getColorAsInt() {
        String color = getColorHtml();
        try {
            // Remove the # prefix
            String hex = color.substring(1);

            // Handle short format (#RGB -> #RRGGBB)
            if (hex.length() == 3) {
                char r = hex.charAt(0);
                char g = hex.charAt(1);
                char b = hex.charAt(2);
                hex = "" + r + r + g + g + b + b;
            }

            // Parse only RGB portion (ignore alpha if present)
            if (hex.length() >= 6) {
                return Integer.parseInt(hex.substring(0, 6), 16);
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return 0xFFFFFF; // Default white
    }

    public int[] getColorRGB() {
        String color = getColorHtml();
        if (color == null || color.isEmpty()) {
            return new int[] {255, 255, 255};
        }
        try {
            String hex = color.startsWith("#") ? color.substring(1) : color;
            int rgb = Integer.parseInt(hex, 16);
            return new int[] {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
        } catch (NumberFormatException e) {
            return new int[] {255, 255, 255};
        }
    }

    /**
     * Get the color components as float array [r, g, b] in range 0.0-1.0.
     *
     * @return Float array with RGB components
     */
    public float[] getColorAsFloats() {
        int rgb = getColorAsInt();
        return new float[] {
                ((rgb >> 16) & 0xFF) / 255.0f,
                ((rgb >> 8) & 0xFF) / 255.0f,
                (rgb & 0xFF) / 255.0f
        };
    }

    /**
     * Get the fallback name with sensible default.
     *
     * @return Fallback name, or ID if not set
     */
    public String getFallbackName() {
        if (fallbackName != null && !fallbackName.isEmpty()) {
            return fallbackName;
        }
        if (id != null && !id.isEmpty()) {
            return formatIDAsName(id);
        }
        return "Unknown " + getFallbackTitle();
    }

    /**
     * Validate if a string is a valid HTML hex color.
     *
     * @param color The color string to validate
     * @return true if valid HTML hex color format
     */
    public static boolean isValidHtmlColor(String color) {
        if (color == null || color.isEmpty()) {
            return false;
        }
        return HTML_COLOR_PATTERN.matcher(color).matches();
    }

    /**
     * Default color (white) when no valid color is specified.
     */
    protected String getDefaultColor() {
        return "#FFFFFF";
    }

    public static String getColor(AbstractTriggerDefinition instance) {
        return instance.getDefaultColor();
    }
}
