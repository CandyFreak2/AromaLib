package com.ovrtechnology;

import com.ovrtechnology.biome.BiomeDefinitionLoader;
import com.ovrtechnology.block.BlockDefinitionLoader;
import com.ovrtechnology.category.CategoryDefinitionLoader;
import com.ovrtechnology.command.AromaTestCommand;
import com.ovrtechnology.data.ResourceManagerDataSource;
import com.ovrtechnology.flower.FlowerDefinitionLoader;
import com.ovrtechnology.hooks.ServerHooks;
import com.ovrtechnology.lookup.LookupManager;
import com.ovrtechnology.mob.MobDefinitionLoader;
import com.ovrtechnology.network.ScentEventNetworking;
import com.ovrtechnology.registry.ModSounds;
import com.ovrtechnology.scent.ScentDefinitionLoader;
import com.ovrtechnology.scent.ScentRegistry;
import com.ovrtechnology.structure.StructureDefinitionLoader;
import com.ovrtechnology.trigger.ScentTriggerManager;
import com.ovrtechnology.trigger.StructureSyncHandler;
import com.ovrtechnology.trigger.config.ScentTriggerConfigLoader;
import com.ovrtechnology.trigger.event.EventDefinitionLoader;
import com.ovrtechnology.trigger.event.EventTriggersConfig;
import com.ovrtechnology.trigger.event.ServerEventBusHandler;
import lombok.experimental.UtilityClass;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.minecraft.core.registries.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Aroma Affect mod.
 * This mod integrates OVR's scent hardware into Minecraft through the "Nose"
 * system.
 *
 * <p>Invoked via {@code Balm.initializeMod(MOD_ID, loadContext, AromaAffect::initialize)}
 * from each platform entry point.</p>
 */
@UtilityClass
public final class AromaLib {
    public static final String MOD_ID = "aromalib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize(BalmRegistrars registrars) {
        LOGGER.info("Initializing Aroma Affect...");

        // === Pure JSON loaders (no registry / no events) =====================
        ScentRegistry.init();
        ScentDefinitionLoader.loadAllScents();
        BiomeDefinitionLoader.loadAllBiomes();
        BlockDefinitionLoader.loadAllBlocks();
        FlowerDefinitionLoader.loadAllFlowers();
        StructureDefinitionLoader.loadAllStructures();
        MobDefinitionLoader.loadAllMobs();
        CategoryDefinitionLoader.loadAllCategories();
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
                                    ScentDefinitionLoader.loadAllScents(ds);
                                    BiomeDefinitionLoader.loadAllBiomes(ds);
                                    BlockDefinitionLoader.loadAllBlocks(ds);
                                    FlowerDefinitionLoader.loadAllFlowers(ds);
                                    StructureDefinitionLoader.loadAllStructures(ds);
                                    MobDefinitionLoader.loadAllMobs(ds);
                                    EventDefinitionLoader.loadAllEvents(ds);
                                    CategoryDefinitionLoader.loadAllCategories(ds);
                                }));



        // === Phase 5 — event listeners  ===============
        LookupManager.init();
        //ActivePathManager.init();
        //AbilityHandler.init();
        StructureSyncHandler.init();
        AromaTestCommand.init();

        LOGGER.info("Aroma Affect initialized successfully!");
    }
}
