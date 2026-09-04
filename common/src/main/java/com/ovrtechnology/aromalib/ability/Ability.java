package com.ovrtechnology.aromalib.ability;

import net.minecraft.server.level.ServerPlayer;

/**
 * Base interface for all abilities in Aroma Affect.
 * 
 * <p>
 * Abilities are special actions that can be performed by players
 * when wearing an item with the corresponding ability unlocked.
 * </p>
 * 
 * <p>
 * Each ability has a unique ID that must match the strings defined
 * in the JSON configuration files .
 * </p>
 *
 * @see AbilityRegistry
 */
public interface Ability {

    /**
     * Gets the unique identifier for this ability.
     * 
     * <p>
     * This ID must match the ability strings defined in the item
     * configuration files (data/aromalib/aroma/item/noses.json).
     * </p>
     * 
     * @return the ability ID
     */
    String getId();

    /**
     * Checks if a player can currently use this ability.
     * 
     * <p>
     * This typically involves checking:
     * </p>
     * <ul>
     * <li>Player has an item equipped with this ability</li>
     * <li>Ability is not on cooldown</li>
     * <li>Any other ability-specific requirements</li>
     * </ul>
     * 
     * @param player the server player to check
     * @return true if the player can use this ability
     */
    boolean canUse(ServerPlayer player);
}
