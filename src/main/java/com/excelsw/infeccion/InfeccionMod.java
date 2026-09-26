package com.excelsw.infeccion;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.effect.ModEffects;
import com.excelsw.infeccion.entity.ModEntities;
import com.excelsw.infeccion.item.ModItems;
import net.fabricmc.api.ModInitializer;
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
        LOGGER.info("La infección ha comenzado...");
    }
}
