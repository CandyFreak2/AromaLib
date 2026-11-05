package com.ovrtechnology.aromalib;

import com.ovrtechnology.aromalib.ability.AbilityHandler;
import com.ovrtechnology.aromalib.ability.AbilityRegistry;
import com.ovrtechnology.aromalib.command.path.ActivePathManager;
import com.ovrtechnology.aromalib.client.config.AromaConfig;
import com.ovrtechnology.aromalib.definition.loader.*;
import com.ovrtechnology.aromalib.command.AromaTestCommand;
import com.ovrtechnology.aromalib.data.ResourceManagerDataSource;
import com.ovrtechnology.aromalib.hooks.ServerHooks;
import com.ovrtechnology.aromalib.lookup.LookupManager;
import com.ovrtechnology.aromalib.network.PathScentNetworking;
import com.ovrtechnology.aromalib.network.ScentEventNetworking;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import com.ovrtechnology.aromalib.registry.TrackerRegistry;
import com.ovrtechnology.aromalib.ability.TrackingAbilityResolver;
import com.ovrtechnology.aromalib.registry.variants.TrackerVariantRegistry;
import com.ovrtechnology.aromalib.trigger.ScentTriggerManager;
import com.ovrtechnology.aromalib.trigger.StructureSyncHandler;
import com.ovrtechnology.aromalib.definition.trigger.ScentTriggerConfigLoader;
import com.ovrtechnology.aromalib.trigger.event.EventTriggersConfig;
import com.ovrtechnology.aromalib.trigger.event.ServerEventBusHandler;
import lombok.experimental.UtilityClass;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public final class AromaLib {
    public static final String MOD_ID = "aromalib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize(BalmRegistrars registrars) {
        LOGGER.info("Initializing Aroma Lib...");

        Balm.config().registerConfig(AromaConfig.class);

        AbilityDefinitionLoader.INSTANCE.loadAll();
        ScentRegistry.INSTANCE.init();
        BiomeDefinitionLoader.INSTANCE.loadAll();
        BlockDefinitionLoader.INSTANCE.loadAll();
        FlowerDefinitionLoader.INSTANCE.loadAll();
        StructureDefinitionLoader.INSTANCE.loadAll();
        MobDefinitionLoader.INSTANCE.loadAll();
        EventDefinitionLoader.loadAllEvents();
        CategoryDefinitionLoader.INSTANCE.loadAll();
        ScentTriggerConfigLoader.init();
        ScentTriggerManager.init();

        registrars.registrar(Registries.ITEM, TrackerRegistry::register);

        TrackerRegistry.INSTANCE.init();
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
                                (ResourceManager rm) -> {
                                    var ds = new ResourceManagerDataSource(rm);
                                    ScentRegistry.INSTANCE.reload(ds);
                                    AbilityDefinitionLoader.INSTANCE.loadAll(ds);
                                    BiomeDefinitionLoader.INSTANCE.loadAll(ds);
                                    BlockDefinitionLoader.INSTANCE.loadAll(ds);
                                    FlowerDefinitionLoader.INSTANCE.loadAll(ds);
                                    StructureDefinitionLoader.INSTANCE.loadAll(ds);
                                    MobDefinitionLoader.INSTANCE.loadAll(ds);
                                    CategoryDefinitionLoader.INSTANCE.loadAll(ds);
                                    TrackerDefinitionLoader.INSTANCE.loadAll(ds);
                                    TrackerVariantRegistry.reload(ds);
                                    TrackingAbilityResolver.rebuild();
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
