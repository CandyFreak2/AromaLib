package com.ovrtechnology.aromalib.definition.loader;

import com.ovrtechnology.aromalib.definition.CategoryDefinition;

public class CategoryDefinitionLoader extends AbstractDefinitionLoader<CategoryDefinition> {
    public static final CategoryDefinitionLoader INSTANCE = new CategoryDefinitionLoader();

    public CategoryDefinitionLoader() {
        super("category", CategoryDefinition.class, "categories");
    }

    @Override
    protected void validate(CategoryDefinition def) {
        String id = def.getId();

        String rawColor = def.getRawColorHtml();
        if (rawColor == null || rawColor.isEmpty()) {
            addWarning("[" + id + "] No color_html defined, using default: " + CategoryDefinition.getColor(def));
        } else if (!CategoryDefinition.isValidHtmlColor(rawColor)) {
            addWarning("[" + id + "] Invalid color_html format '" + rawColor + "', using default: " + CategoryDefinition.getColor(def));
        }

        if (!id.contains(":")) {
            addWarning("[" + id + "] ID should include namespace (e.g., 'minecraft:stone')");
        }
    }
}
