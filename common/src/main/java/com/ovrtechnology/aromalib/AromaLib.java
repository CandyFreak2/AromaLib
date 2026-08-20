package com.ovrtechnology.aromalib;

import com.ovrtechnology.aromalib.definition.loader.BiomeDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.BlockDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.CategoryDefinitionLoader;
import com.ovrtechnology.aromalib.command.AromaTestCommand;
import com.ovrtechnology.aromalib.data.ResourceManagerDataSource;
import com.ovrtechnology.aromalib.definition.loader.FlowerDefinitionLoader;
import com.ovrtechnology.aromalib.hooks.ServerHooks;
import com.ovrtechnology.aromalib.lookup.LookupManager;
import com.ovrtechnology.aromalib.definition.loader.MobDefinitionLoader;
import com.ovrtechnology.aromalib.network.ScentEventNetworking;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import com.ovrtechnology.aromalib.definition.loader.StructureDefinitionLoader;
import com.ovrtechnology.aromalib.trigger.ScentTriggerManager;
import com.ovrtechnology.aromalib.trigger.StructureSyncHandler;
import com.ovrtechnology.aromalib.trigger.config.ScentTriggerConfigLoader;
import com.ovrtechnology.aromalib.definition.loader.EventDefinitionLoader;
import com.ovrtechnology.aromalib.trigger.event.EventTriggersConfig;
import com.ovrtechnology.aromalib.trigger.event.ServerEventBusHandler;
import lombok.experimental.UtilityClass;
import net.blay09.mods.balm.core.BalmRegistrars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public final class AromaLib {
    public static final String MOD_ID = "aromalib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize(BalmRegistrars registrars) {
        LOGGER.info("Initializing Aroma Lib...");

        // === Pure JSON loaders (no registry / no events) =====================
        ScentRegistry.init();
        BiomeDefinitionLoader.loadAllBiomes();
        BlockDefinitionLoader.loadAllBlocks();
        FlowerDefinitionLoader.loadAllFlowers();
        StructureDefinitionLoader.loadAllStructures();
        MobDefinitionLoader.loadAllMobs();
        EventDefinitionLoader.loadAllEvents();
        CategoryDefinitionLoader.loadAllCategories();
        ScentTriggerConfigLoader.init();
        ScentTriggerManager.init();

        // === Phase 6 — networking  ===================
        ScentEventNetworking.init();

        // === Event-trigger scent system =====================================
        EventTriggersConfig.getInstance();
        ServerEventBusHandler.init();
        ServerHooks.register();
        // Trackable content + event definitions live in per-file directories and
        // are enumerated via the ResourceManager, so they load on server-data reload
        // (world load + /reload). This is what picks up modpack datapack additions.
        registrars.resourceReloadListeners(
                reg ->
                        reg.register(
                                MOD_ID + "_data_reload",
                                (net.minecraft.server.packs.resources.ResourceManager rm) -> {
                                    var ds = new ResourceManagerDataSource(rm);
                                    ScentRegistry.reload(ds);
                                    BiomeDefinitionLoader.loadAllBiomes(ds);
                                    BlockDefinitionLoader.loadAllBlocks(ds);
                                    FlowerDefinitionLoader.loadAllFlowers(ds);
                                    StructureDefinitionLoader.loadAllStructures(ds);
                                    MobDefinitionLoader.loadAllMobs(ds);
                                    CategoryDefinitionLoader.loadAllCategories(ds);
                                    EventDefinitionLoader.loadAllEvents(ds);
                                    ScentTriggerConfigLoader.rebuild();
                                }));



        // === Phase 5 — event listeners  ===============
        LookupManager.init();
        StructureSyncHandler.init();
        AromaTestCommand.init();

        LOGGER.info("Aroma Lib initialized successfully!");
    }
}
