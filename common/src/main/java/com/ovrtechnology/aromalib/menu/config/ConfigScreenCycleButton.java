package com.ovrtechnology.aromalib.menu.config;

import net.blay09.mods.balm.client.platform.config.screen.BalmConfigScreenRowState;
import net.blay09.mods.balm.platform.config.schema.ConfiguredProperty;
import net.blay09.mods.balm.platform.config.util.ConfigLocalization;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigScreenCycleButton<T> extends CycleButton<T> {
    public ConfigScreenCycleButton(int x, int y, int width, int height, Component message, Component name, int index, T value, Supplier<T> supplier, ValueListSupplier<T> values, Function<T, Component> valueStringifier, Function<CycleButton<T>, MutableComponent> narrationProvider, OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, DisplayState displayState, SpriteSupplier<T> spriteSupplier) {
        super(x, y, width, height, message, name, index, value, supplier, values, valueStringifier, narrationProvider, onValueChange, tooltipSupplier, displayState, spriteSupplier);
    }

    public static CycleButton<Boolean> create(boolean initialValue, int x, int y, int width, int height, Component name, OnValueChange<Boolean> valueChangeListener) {
        return onOffBuilder(initialValue).displayOnlyValue().create(x, y, width, height, name, valueChangeListener);
    }

    public static CycleButton<Boolean> create(ConfiguredProperty<Boolean> property, BalmConfigScreenRowState state) {
        final var label = Component.translatable(ConfigLocalization.forProperty(property));
        return create(property.defaultValue(), 0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, label,
                (_, value) -> state.set(value));
    }
}
