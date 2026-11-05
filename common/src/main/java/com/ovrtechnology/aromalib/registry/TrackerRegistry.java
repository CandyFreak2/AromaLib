package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.TrackerDefinitionLoader;
import com.ovrtechnology.aromalib.ability.TrackingAbilityResolver;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.item.tracker.TrackingItem;
import com.ovrtechnology.aromalib.item.tracker.ITrackableItem;
import lombok.Getter;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.*;

public class TrackerRegistry extends AbstractRegistry<TrackerDefinition> {
    @Getter private static final Map<String, Holder<Item>> items = new LinkedHashMap<>();

    public static final TrackerRegistry INSTANCE = new TrackerRegistry();
    public static final TrackerDefinitionLoader LOADER = new TrackerDefinitionLoader();

    public TrackerRegistry() {
        super("tracker");
    }

    @Override
    public void init() {
        TrackingAbilityResolver.init();
    }

    @Override
    protected AbstractRegistry<TrackerDefinition> getInstance() {
        return INSTANCE;
    }

    @Override
    protected AbstractDefinitionLoader<TrackerDefinition> getLoader() {
        return LOADER;
    }

    public static void register(BalmRegistrar.Scoped<Item> items) {
        if (INSTANCE.isInitialized()) {
            AromaLib.LOGGER.warn("TrackerRegistry.register() called multiple times!");
            return;
        }

        AromaLib.LOGGER.info("Initializing TrackerRegistry...");

        List<TrackerDefinition> definitions = TrackerDefinitionLoader.INSTANCE.loadAll();
        for (TrackerDefinition definition : definitions) {
            register(items, definition);
        }

        INSTANCE.initialized = true;
        AromaLib.LOGGER.info("TrackerRegistry initialized with {} trackers", TrackerRegistry.items.size());
    }

    private static void register(BalmRegistrar.Scoped<Item> items, TrackerDefinition definition) {
        String id = definition.getId();

        if (TrackerRegistry.items.containsKey(id)) {
            AromaLib.LOGGER.warn("Duplicate tracker ID: {}, skipping...", id);
            return;
        }

        INSTANCE.definitions.put(id, definition);

        final String itemId = id;
        final EquipmentSlot slot = EquipmentSlot.byName(definition.getSlot());
        Holder<Item> holder = items.register(id, _ -> new TrackingItem(definition, itemId, slot));
        TrackerRegistry.items.put(id, holder);

        AromaLib.LOGGER.debug("Registered tracker item: {}", id);
    }

    public Optional<ITrackableItem> getItem(String id) {
        Holder<Item> holder = items.get(id);
        if (holder != null && holder.isBound() && holder.value() instanceof ITrackableItem trackerItem) {
            return Optional.of(trackerItem);
        }
        return Optional.empty();
    }

    public static Optional<Holder<Item>> getHolder(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public static Iterable<Holder<Item>> getAllItems() {
        return items.values();
    }

    public static List<ITrackableItem> getAllItemsAsList() {
        List<ITrackableItem> result = new ArrayList<>();
        for (Holder<Item> holder : items.values()) {
            if (holder.isBound() && holder.value() instanceof ITrackableItem item) {
                result.add(item);
            }
        }
        return result;
    }
}
