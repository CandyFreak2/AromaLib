package com.ovrtechnology.aromalib.registry.variants;

import com.ovrtechnology.aromalib.definition.AbstractTriggerDefinition;
import com.ovrtechnology.aromalib.definition.ScentDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.registry.AbstractRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTriggerDefinitionRegistry<T extends AbstractTriggerDefinition> extends AbstractRegistry<T> {
    public AbstractTriggerDefinitionRegistry(String name) {
        super(name);
    }

    protected abstract AbstractRegistry<T> getInstance();

    protected abstract AbstractDefinitionLoader<T> getLoader();

    public Optional<AbstractTriggerDefinition> getDefinitionByName(String name) {
        if (name == null) return Optional.empty();
        for (AbstractTriggerDefinition def : definitions.values()) {
            if (name.equalsIgnoreCase(def.getFallbackName())) {
                return Optional.of(def);
            }
        }
        return Optional.empty();
    }

    public List<AbstractTriggerDefinition> getDefinitionByScent(String scent) {
        List<AbstractTriggerDefinition> result = new ArrayList<>();
        for (AbstractTriggerDefinition def : definitions.values()) {
            if (scent.equals(def.getId())) {
                result.add(def);
            }
        }
        return result;
    }

    public int getColor(String id) {
        T def = definitions.get(id);
        return def != null ? def.getColorAsInt() : 0x808080;
    }

    public String getColorHTML(String id) {
        T def = definitions.get(id);
        return def != null ? def.getColorHtml() : AbstractTriggerDefinition.getColor(def);
    }

    public String getDisplayName(String id) {
        T def = definitions.get(id);
        if (def == null) {
            return "Unknown " + getName() ;
        }
        // TODO: Integrate with Minecraft's localization system
        // For now, return the fallback name
        return def.getFallbackName();
    }
}
