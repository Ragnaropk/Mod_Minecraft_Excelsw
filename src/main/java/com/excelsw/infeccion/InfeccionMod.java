package com.excelsw.infeccion;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.effect.ModEffects;
import com.excelsw.infeccion.entity.ModEntities;
import com.excelsw.infeccion.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfeccionMod implements ModInitializer {
    public static final String MOD_ID = "infeccion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModEffects.register();
        ModBlocks.register();
        ModEntities.register();
        ModItems.register();
        ModGameRules.register();
        InfectionEvents.register();

        // El fuego quema la infección.
        FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
        fire.add(ModBlocks.INFECTED_LOG, 5, 5);
        fire.add(ModBlocks.INFECTED_LEAVES, 30, 60);
        fire.add(ModBlocks.INFECTED_GROWTH, 60, 100);
        LOGGER.info("La infección ha comenzado...");
    }
}
