package com.ovrtechnology.aromalib.definition.variant;

import com.google.gson.JsonObject;

import java.util.Map;

public class Variant extends AbstractVariant {
    public Variant(JsonObject conditions, String scentID, String perception) {
        this.conditions = conditions;
        this.scentId = scentID;
        this.perception = perception;
    }
}
