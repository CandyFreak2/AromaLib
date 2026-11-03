package com.ovrtechnology.aromalib.menu.config;

import lombok.Getter;
import net.blay09.mods.balm.client.platform.config.screen.BalmConfigScreenRowState;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMergeContainer extends AbstractContainerWidget {
    @Getter private final List<BalmConfigScreenRowState> states = new ArrayList<>();

    public AbstractMergeContainer() {
        super(0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.empty());
    }

    public AbstractMergeContainer(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public BalmConfigScreenRowState getOrCreate(int i) {
        while (states.size() <= i) {
            states.add(new BalmConfigScreenRowState());
        }
        return states.get(i);
    }

    public List<BalmConfigScreenRowState> all() {
        return List.copyOf(states);
    }
}
