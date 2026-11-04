package com.ovrtechnology.aromalib.definition.variant;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public abstract class AbstractVariant {
    @SerializedName("conditions")
    protected JsonObject conditions;
    @SerializedName("scent_id")
    protected String scentId;
    @SerializedName("perception")
    protected String perception;
}
