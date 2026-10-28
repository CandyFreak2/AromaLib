package com.ovrtechnology.aromalib.definition;

import com.google.gson.annotations.SerializedName;
import com.ovrtechnology.aromalib.ability.Ability;
import com.ovrtechnology.aromalib.util.AbilityType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents an ability definition loaded from JSON.
 *
 * <p>
 * Abilities are special actions that can be granted to items. Each ability
 * has a type that determines how it's triggered and a config object for
 * ability-specific settings.
 * </p>
 *
 * <h2>Ability Types:</h2>
 * <ul>
 * <li><b>passive</b>: Always active while wearing the item</li>
 * <li><b>block_interaction</b>: Activated by right-clicking blocks</li>
 * </ul>
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class AbilityDefinition extends Definition {
    @SerializedName("type")
    private AbilityType type;

    @SerializedName("description")
    private String description;

    @SerializedName("config")
    private Map<String, Object> config;

    public AbilityDefinition() {
        super();
    }

    public AbilityDefinition(String id) {
        super(id);
    }

    public String getType() {
        return type != null ? type.getText() : AbilityType.PASSIVE.getText();
    }

    public boolean isTypeValid() {
        return Arrays.stream(AbilityType.values()).anyMatch(t -> t.equals(getType()));
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public Map<String, Object> getConfig() {
        return config != null ? config : Collections.emptyMap();
    }

    /**
     * Gets an integer config value with a default.
     *
     * @param key          the config key
     * @param defaultValue the default if not found
     * @return the config value or default
     */
    public int getConfigInt(String key, int defaultValue) {
        Object value = getConfig().get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Gets a double config value with a default.
     *
     * @param key          the config key
     * @param defaultValue the default if not found
     * @return the config value or default
     */
    public double getConfigDouble(String key, double defaultValue) {
        Object value = getConfig().get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Gets a string config value with a default.
     *
     * @param key          the config key
     * @param defaultValue the default if not found
     * @return the config value or default
     */
    public String getConfigString(String key, String defaultValue) {
        Object value = getConfig().get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * Gets a boolean config value with a default.
     *
     * @param key          the config key
     * @param defaultValue the default if not found
     * @return the config value or default
     */
    public boolean getConfigBoolean(String key, boolean defaultValue) {
        Object value = getConfig().get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Gets a list of strings config value.
     *
     * @param key the config key
     * @return the list or empty list if not found
     */
    @SuppressWarnings("unchecked")
    public List<String> getConfigStringList(String key) {
        Object value = getConfig().get(key);
        if (value instanceof List<?>) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }

    /**
     * Checks if this ability is a passive type.
     *
     * @return true if passive
     */
    public boolean isPassive() {
        return AbilityType.PASSIVE.equals(getType());
    }

    /**
     * Checks if this ability is a block interaction type.
     *
     * @return true if block interaction
     */
    public boolean isBlockInteraction() {
        return AbilityType.BLOCK_INTERACTION.equals(getType());
    }
}
