package com.ovrtechnology.aromalib.data;

import com.ovrtechnology.aromalib.AromaLib;

public final class DataSourceInfo {
    public static final String DATA_ROOT = "data/" + AromaLib.MOD_ID + "/";
    public static final String DIRECTORY = "aroma/";
    public static final String BASE = DATA_ROOT + DIRECTORY;

    public static final String TEXTURE_ROOT = "assets/" + AromaLib.MOD_ID + "/";
    public static final String MASK_DIRECTORY = "textures/mask";
    public static final String MASK = TEXTURE_ROOT + MASK_DIRECTORY;
}
