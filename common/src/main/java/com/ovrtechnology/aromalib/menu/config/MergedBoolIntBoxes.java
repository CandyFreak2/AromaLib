package com.ovrtechnology.aromalib.menu.config;

import net.blay09.mods.balm.client.platform.config.screen.BalmConfigScreenContext;
import net.blay09.mods.balm.client.platform.config.screen.BalmConfigScreenEditBox;
import net.blay09.mods.balm.platform.config.schema.ConfiguredProperty;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.List;

public class MergedBoolIntBoxes extends AbstractMergeContainer {
    private static final int GAP = 8;
    private final CycleButton<Boolean> first;
    private final BalmConfigScreenEditBox<Integer> second;
    private final List<GuiEventListener> children;

    public MergedBoolIntBoxes(BalmConfigScreenContext context, ConfiguredProperty<Boolean> first, ConfiguredProperty<Integer> second) {
        super();

        var firstState = getOrCreate(0);
        var secondState = getOrCreate(1);

        this.first = ConfigScreenCycleButton.create(first, firstState);
        this.second = new BalmConfigScreenEditBox<>(context.font(), second, context, secondState);

        children = List.of(this.first, this.second);
        layoutChildren();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layoutChildren();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        layoutChildren();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        layoutChildren();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        layoutChildren();
    }

    private void layoutChildren() {
        final var editBoxWidth = Math.max(1, (width - GAP) / 2);
        first.setPosition(getX(), getY());
        first.setSize(editBoxWidth, height);
        second.setPosition(getX() + editBoxWidth + GAP, getY());
        second.setSize(width - editBoxWidth - GAP, height);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        first.active = active;
        second.active = active;
        first.visible = visible;
        second.visible = visible;
        first.extractRenderState(graphics, mouseX, mouseY, partialTick);
        second.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected int contentHeight() {
        return height;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}

