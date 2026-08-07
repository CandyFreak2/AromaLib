package com.ovrtechnology.aromalib.trigger.event;

/**
 * Duck interface merged onto {@code AbstractFurnaceBlockEntity} so the static {@code serverTick}
 * mixin can remember a furnace's previous lit state across ticks and detect the ignition edge.
 *
 * <p>Deliberately kept outside {@code com.ovrtechnology.mixin} — classes in the mixin-owned
 * package cannot be referenced directly at runtime (Mixin throws IllegalClassLoadError).</p>
 */
public interface FurnaceLitAccess {

    boolean aromalib$wasLit();

    void aromalib$setWasLit(boolean wasLit);
}
