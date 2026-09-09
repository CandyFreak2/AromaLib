package com.ovrtechnology.aromalib.tracking;

import com.ovrtechnology.aromalib.registry.ModSounds;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side singleton that holds the currently active tracking target.
 *
 * <p>Only one target can be tracked at a time across all categories (blocks, biomes,
 * structures, flowers). All selection menu screens write to this state when the
 * player picks a target, and clear it on deselection or path stop.</p>
 *
 * <p>The radial menu reads this state to display an active tracking info panel
 * and to mark which category slice has an active track.</p>
 */
public final class ActiveTrackingState {

    /**
     * Tracking status state machine.
     *
     * <pre>
     * IDLE → SEARCHING → TRACKING → ARRIVED → (auto-clear) → IDLE
     *                   → NOT_FOUND → (auto-clear) → IDLE
     *                   → ERROR → (auto-clear) → IDLE
     * </pre>
     */
    public enum TrackingStatus {
        IDLE,
        SEARCHING,
        TRACKING,
        ARRIVED,
        NOT_FOUND,
        ERROR
    }

    @Getter
    private static Identifier targetId;

    @Getter
    private static Component displayName;

    @Getter
    private static ItemStack icon;

    @Getter
    private static TrackingCategory category;

    @Getter
    @Setter
    private static int distance = -1;

    @Getter
    private static BlockPos destination;

    @Getter
    private static TrackingStatus status = TrackingStatus.IDLE;

    @Getter
    private static String statusMessage;
    private static long statusTimestamp;
    private static Identifier lastDimensionId;

    /** Duration in milliseconds before terminal states auto-clear. */
    private static final long AUTO_CLEAR_MS = 3000;

    /** Duration in milliseconds before ARRIVED state auto-clears (keeps outline visible longer). */
    private static final long ARRIVED_CLEAR_MS = 8000;

    private ActiveTrackingState() {}

    /**
     * Sets the currently tracked target and transitions to SEARCHING state.
     *
     * @param id       resource location of the target (e.g. minecraft:dandelion)
     * @param name     display name shown in the UI
     * @param itemIcon item icon for the target
     * @param cat      the category this target belongs to
     */
    public static void set(Identifier id, Component name, ItemStack itemIcon, TrackingCategory cat) {
        targetId = id;
        displayName = name;
        icon = itemIcon;
        category = cat;
        distance = -1;
        status = TrackingStatus.SEARCHING;
        statusMessage = null;
        statusTimestamp = System.currentTimeMillis();

        // Play random sniff sound
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(ModSounds.SNIFF.value(), 0.8f, 1.0f);
        }
    }

    /**
     * Transitions to TRACKING state when the server confirms a path was found.
     *
     * @param dist initial distance in blocks
     */
    public static void setTracking(int dist) {
        setTracking(dist, null);
    }

    public static void setTracking(int dist, BlockPos dest) {
        status = TrackingStatus.TRACKING;
        distance = dist;
        destination = dest;
        statusMessage = null;
        statusTimestamp = System.currentTimeMillis();
    }

    /**
     * Transitions to ARRIVED state. Auto-clears after 3 seconds.
     */
    public static void setArrived() {
        status = TrackingStatus.ARRIVED;
        // Keep destination so the block outline stays visible during ARRIVED state
        statusMessage = null;
        statusTimestamp = System.currentTimeMillis();
    }

    /**
     * Transitions to NOT_FOUND or ERROR state with an optional reason message.
     * Auto-clears after 3 seconds.
     *
     * @param reason human-readable reason for the failure
     * @param isError true for ERROR state, false for NOT_FOUND
     */
    public static void setFailed(String reason, boolean isError) {
        status = isError ? TrackingStatus.ERROR : TrackingStatus.NOT_FOUND;
        statusMessage = reason;
        statusTimestamp = System.currentTimeMillis();
    }

    /**
     * Clears the active tracking state, resetting to IDLE.
     */
    public static void clear() {
        targetId = null;
        displayName = null;
        icon = null;
        category = null;
        distance = -1;
        destination = null;
        status = TrackingStatus.IDLE;
        statusMessage = null;
    }

    /**
     * Called each client tick to auto-clear terminal states (ARRIVED, NOT_FOUND, ERROR)
     * after {@link #AUTO_CLEAR_MS} milliseconds.
     */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Identifier currentDimensionId = mc.player.level().dimension().identifier();
            if (lastDimensionId != null && !lastDimensionId.equals(currentDimensionId) && status != TrackingStatus.IDLE) {
                clear();
            }
            lastDimensionId = currentDimensionId;
        } else {
            lastDimensionId = null;
        }

        if (status == TrackingStatus.ARRIVED) {
            if (System.currentTimeMillis() - statusTimestamp > ARRIVED_CLEAR_MS) {
                clear();
            }
        } else if (status == TrackingStatus.NOT_FOUND || status == TrackingStatus.ERROR) {
            if (System.currentTimeMillis() - statusTimestamp > AUTO_CLEAR_MS) {
                clear();
            }
        }
    }

    /**
     * @return true if a target is currently being tracked (any non-IDLE state)
     */
    public static boolean isTracking() {
        return status != TrackingStatus.IDLE;
    }

    /**
     * @return true if actively tracking with a live path (TRACKING state only)
     */
    public static boolean isActivelyTracking() {
        return status == TrackingStatus.TRACKING;
    }

    /**
     * @return true if the given target ID is the one currently being tracked
     */
    public static boolean isTracking(Identifier id) {
        return targetId != null && targetId.equals(id);
    }

    /**
     * @return the category ID string of the tracked target, or null
     */
    public static String getCategoryId() {
        return category != null ? category.getId() : null;
    }

    /**
     * @return true if a block outline should be rendered for the current tracking target
     */
    public static boolean shouldShowOutline() {
        return (status == TrackingStatus.TRACKING || status == TrackingStatus.ARRIVED)
                && destination != null
                && distance >= 0 && distance <= 32
                && category != null && category.hasWorldOutline();
    }
}
