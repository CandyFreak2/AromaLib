package com.ovrtechnology.aromalib.definition;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefinitionRegistry<T extends IDefinition> {
    @Getter
    private final List<T> loadedDefinitions;
    private final Map<String, T> idMap;


    public DefinitionRegistry(List<T> loadedDefinitions, Map<String, T> idMap) {
        this.loadedDefinitions = Collections.unmodifiableList(loadedDefinitions);
        this.idMap = Collections.unmodifiableMap(idMap);
    }

    public T getByID(String id) {
        return idMap.get(id);
    }

    public boolean hasID(String key) {
        return idMap.containsKey(key);
    }
}
