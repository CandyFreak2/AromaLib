package com.ovrtechnology.aromalib.registry.variants;

import com.ovrtechnology.aromalib.definition.AbstractDefinition;
import com.ovrtechnology.aromalib.definition.ScentedDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.ScentDefinitionLoader;
import com.ovrtechnology.aromalib.registry.AbstractRegistry;

public class AbstractScentRegistry /*extends AbstractRegistry<ScentDefinition, AbstractDefinitionLoader>*/ {


    // @TODO abstract for scented definitions
//    public Optional<T> getScentForDefinition(String id) {
//        T def = definitions.get(id);
//        if (def instanceof ScentedDefinition scentDef) {
//            if (scentDef.hasScentId()) {
//                return ScentRegistry.getScent(scentDef.getScentId());
//            }
//            return Optional.empty();
//        }
//        return Optional.empty();
//    }
//    public List<T> getDefinitionByScent(String scent) {
//        List<T> result = new ArrayList<>();
//        for (T def : definitions.values()) {
//            if (scent.equals(def.getScentId())) {
//                result.add(def);
//            }
//        }
//        return result;
//    }

    // @ TODO
//    public int getColor(String id) {
//        T def = definitions.get(id);
//        return def != null ? def.getColorAsInt() : 0x808080;
//    }
// public String getColorHTML(String id) {
//        T def = definitions.get(id);
//        return def != null ? def.getColorHtml() : ScentedDefinition.getColor(def);
//    }
}
