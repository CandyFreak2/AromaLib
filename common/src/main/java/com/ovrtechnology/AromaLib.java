package com.ovrtechnology;

import lombok.experimental.UtilityClass;
import net.blay09.mods.balm.core.BalmRegistrars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Aroma Affect mod.
 * This mod integrates OVR's scent hardware into Minecraft through the "Nose"
 * system.
 *
 * <p>Invoked via {@code Balm.initializeMod(MOD_ID, loadContext, AromaAffect::initialize)}
 * from each platform entry point.</p>
 */
@UtilityClass
public final class AromaLib {
    public static final String MOD_ID = "aromalib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize(BalmRegistrars registrars) {

    }
}
