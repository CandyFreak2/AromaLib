package com.ovrtechnology.registry;

import com.ovrtechnology.AromaLib;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

/**
 * Registry for custom sound events used by Aroma Affect.
 *
 * <p>Registered through {@code registrars.registrar(Registries.SOUND_EVENT, ModSounds::register)}
 * from {@link AromaLib#initialize(net.blay09.mods.balm.core.BalmRegistrars)}.</p>
 */
public final class ModSounds {

    /**
     * Sniff sound played when a tracking target is selected.
     * Minecraft automatically picks a random variant from sounds.json.
     */
    public static Holder<SoundEvent> SNIFF;

    public static Holder<SoundEvent> OMARA_PUFF;

    public static void register(BalmRegistrar.Scoped<SoundEvent> sounds) {
        SNIFF = sounds.register("sniff", SoundEvent::createVariableRangeEvent);
        OMARA_PUFF = sounds.register("omara_puff", SoundEvent::createVariableRangeEvent);
        AromaLib.LOGGER.debug("Registered custom sound events");
    }

    private ModSounds() {}
}
