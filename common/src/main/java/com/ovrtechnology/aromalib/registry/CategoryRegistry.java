package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.definition.CategoryDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.CategoryDefinitionLoader;

public final class CategoryRegistry extends AbstractRegistry<CategoryDefinition> {
    public static final CategoryRegistry INSTANCE = new CategoryRegistry();
    public static final CategoryDefinitionLoader LOADER = new CategoryDefinitionLoader();

    public CategoryRegistry() {
        super("Category");
    }

    @Override
    protected CategoryRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    protected AbstractDefinitionLoader<CategoryDefinition> getLoader() {
        return LOADER;
    }
}

