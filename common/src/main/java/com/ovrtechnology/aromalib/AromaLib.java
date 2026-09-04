package com.ovrtechnology.aromalib;

import com.ovrtechnology.aromalib.ability.AbilityHandler;
import com.ovrtechnology.aromalib.ability.AbilityRegistry;
import com.ovrtechnology.aromalib.command.path.ActivePathManager;
import com.ovrtechnology.aromalib.definition.loader.*;
import com.ovrtechnology.aromalib.command.AromaTestCommand;
import com.ovrtechnology.aromalib.data.ResourceManagerDataSource;
import com.ovrtechnology.aromalib.hooks.ServerHooks;
import com.ovrtechnology.aromalib.lookup.LookupManager;
import com.ovrtechnology.aromalib.network.PathScentNetworking;
import com.ovrtechnology.aromalib.network.ScentEventNetworking;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import com.ovrtechnology.aromalib.trigger.ScentTriggerManager;
import com.ovrtechnology.aromalib.trigger.StructureSyncHandler;
import com.ovrtechnology.aromalib.trigger.config.ScentTriggerConfigLoader;
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

        AbilityDefinitionLoader.loadAllAbilities();
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

        AbilityRegistry.init();

        PathScentNetworking.init();
        ScentEventNetworking.init();

        EventTriggersConfig.getInstance();
        ServerEventBusHandler.init();
        ServerHooks.register();

        registrars.resourceReloadListeners(
                reg ->
                        reg.register(
                                MOD_ID + "_data_reload",
                                (net.minecraft.server.packs.resources.ResourceManager rm) -> {
                                    var ds = new ResourceManagerDataSource(rm);
                                    ScentRegistry.reload(ds);
                                    AbilityDefinitionLoader.loadAllAbilities(ds);
                                    BiomeDefinitionLoader.loadAllBiomes(ds);
                                    BlockDefinitionLoader.loadAllBlocks(ds);
                                    FlowerDefinitionLoader.loadAllFlowers(ds);
                                    StructureDefinitionLoader.loadAllStructures(ds);
                                    MobDefinitionLoader.loadAllMobs(ds);
                                    CategoryDefinitionLoader.loadAllCategories(ds);
                                    EventDefinitionLoader.loadAllEvents(ds);
                                    ScentTriggerConfigLoader.rebuild();
                                }));



        LookupManager.init();
        ActivePathManager.init();
        AbilityHandler.init();
        StructureSyncHandler.init();
        AromaTestCommand.init();

        LOGGER.info("Aroma Lib initialized successfully!");
    }
}
